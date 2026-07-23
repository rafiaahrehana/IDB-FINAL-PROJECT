package com.businessos.modules.finance.invoice;

import com.businessos.modules.crm.client.Client;
import com.businessos.shared.email.EmailBranding;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Renders a ClientInvoice as a downloadable PDF. Builds a small hand-written XHTML
 * string (openhtmltopdf requires well-formed XML input, not lenient HTML5) and
 * converts it with openhtmltopdf/PDFBox - no external template engine needed for
 * a document this simple.
 */
@Service
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public byte[] generate(ClientInvoice invoice, EmailBranding.Data branding) {
        String html = buildHtml(invoice, branding);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate invoice PDF", ex);
        }
        return os.toByteArray();
    }

    private String buildHtml(ClientInvoice invoice, EmailBranding.Data branding) {
        Client client = invoice.getClient();
        String accent = branding.getPrimaryColor() != null ? branding.getPrimaryColor() : "#1e3a5f";
        String clientName = client != null && client.getUser() != null
                ? client.getUser().getFirstName() + " " + client.getUser().getLastName() : "";
        String clientCompany = client != null ? client.getClientCompanyName() : null;
        String clientEmail = client != null && client.getUser() != null ? client.getUser().getEmail() : "";
        String billingAddress = client != null ? client.getBillingAddress() : null;

        StringBuilder rows = new StringBuilder();
        if (invoice.getItems() != null) {
            for (ClientInvoiceItem item : invoice.getItems()) {
                rows.append("<tr>")
                        .append("<td>").append(escape(item.getDescription())).append("</td>")
                        .append("<td class=\"num\">").append(formatQty(item.getQuantity())).append("</td>")
                        .append("<td class=\"num\">").append(formatMoney(item.getUnitPrice())).append("</td>")
                        .append("<td class=\"num\">").append(formatMoney(item.getLineTotal())).append("</td>")
                        .append("</tr>");
            }
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE html>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><meta charset=\"UTF-8\"/><style>"
                + "body{font-family:Helvetica,Arial,sans-serif;color:#1f2937;font-size:11px;}"
                + ".header{display:table;width:100%;margin-bottom:24px;}"
                + ".header .company{display:table-cell;vertical-align:top;}"
                + ".header .title{display:table-cell;text-align:right;vertical-align:top;}"
                + ".company-name{font-size:18px;font-weight:bold;color:" + accent + ";}"
                + ".invoice-title{font-size:24px;font-weight:bold;color:" + accent + ";letter-spacing:2px;}"
                + ".meta{margin-top:4px;color:#6b7280;}"
                + ".section{margin-bottom:18px;}"
                + ".label{color:#6b7280;text-transform:uppercase;font-size:9px;letter-spacing:1px;margin-bottom:2px;}"
                + "table.items{width:100%;border-collapse:collapse;margin-top:8px;}"
                + "table.items th{background:#f3f4f6;text-align:left;padding:6px 8px;font-size:9px;text-transform:uppercase;color:#6b7280;}"
                + "table.items td{padding:6px 8px;border-bottom:1px solid #e5e7eb;}"
                + "table.items .num{text-align:right;}"
                + ".totals{width:260px;margin-left:auto;margin-top:12px;}"
                + ".totals tr td{padding:4px 0;}"
                + ".totals .num{text-align:right;}"
                + ".totals .grand td{font-weight:bold;font-size:13px;border-top:2px solid " + accent + ";padding-top:8px;}"
                + ".status{display:inline-block;padding:3px 10px;border-radius:10px;font-size:10px;font-weight:bold;color:#fff;background:" + accent + ";}"
                + ".footer{margin-top:32px;color:#9ca3af;font-size:9px;border-top:1px solid #e5e7eb;padding-top:8px;}"
                + "</style></head><body>"
                + "<div class=\"header\">"
                + "<div class=\"company\"><div class=\"company-name\">" + escape(branding.getCompanyName()) + "</div></div>"
                + "<div class=\"title\"><div class=\"invoice-title\">INVOICE</div>"
                + "<div class=\"meta\">" + escape(invoice.getInvoiceNumber()) + "</div></div>"
                + "</div>"

                + "<div class=\"header\">"
                + "<div class=\"company\">"
                + "<div class=\"label\">Bill To</div>"
                + "<div>" + escape(clientCompany != null && !clientCompany.isBlank() ? clientCompany : clientName) + "</div>"
                + (clientCompany != null && !clientCompany.isBlank() ? "<div>" + escape(clientName) + "</div>" : "")
                + "<div>" + escape(clientEmail) + "</div>"
                + (billingAddress != null && !billingAddress.isBlank() ? "<div>" + escape(billingAddress) + "</div>" : "")
                + "</div>"
                + "<div class=\"title\">"
                + "<div class=\"label\">Invoice Date</div><div>" + formatDate(invoice.getInvoiceDate()) + "</div>"
                + "<div class=\"label\" style=\"margin-top:8px;\">Due Date</div><div>" + formatDate(invoice.getDueDate()) + "</div>"
                + "<div style=\"margin-top:8px;\"><span class=\"status\">" + escape(invoice.getStatus() != null ? invoice.getStatus().name() : "") + "</span></div>"
                + "</div>"
                + "</div>"

                + (invoice.getDescription() != null && !invoice.getDescription().isBlank()
                    ? "<div class=\"section\"><div class=\"label\">Description</div><div>" + escape(invoice.getDescription()) + "</div></div>"
                    : "")

                + "<table class=\"items\"><thead><tr><th>Description</th><th class=\"num\">Qty</th><th class=\"num\">Unit Price</th><th class=\"num\">Total</th></tr></thead>"
                + "<tbody>" + rows + "</tbody></table>"

                + "<table class=\"totals\">"
                + "<tr><td>Subtotal</td><td class=\"num\">" + formatMoney(invoice.getSubtotal()) + "</td></tr>"
                + (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0
                    ? "<tr><td>Tax</td><td class=\"num\">" + formatMoney(invoice.getTaxAmount()) + "</td></tr>" : "")
                + "<tr class=\"grand\"><td>Total</td><td class=\"num\">" + formatMoney(invoice.getTotalAmount()) + "</td></tr>"
                + "<tr><td>Paid</td><td class=\"num\">" + formatMoney(invoice.getPaidAmount()) + "</td></tr>"
                + "<tr><td>Balance Due</td><td class=\"num\">" + formatMoney(invoice.getBalanceAmount()) + "</td></tr>"
                + "</table>"

                + (invoice.getNotes() != null && !invoice.getNotes().isBlank()
                    ? "<div class=\"section\" style=\"margin-top:20px;\"><div class=\"label\">Notes</div><div>" + escape(invoice.getNotes()) + "</div></div>"
                    : "")

                + "<div class=\"footer\">Generated by " + escape(branding.getCompanyName()) + "</div>"
                + "</body></html>";
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String formatQty(BigDecimal qty) {
        return qty == null ? "1" : qty.stripTrailingZeros().toPlainString();
    }

    private String formatDate(java.time.LocalDate date) {
        return date == null ? "-" : date.format(DATE_FMT);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
