package com.businessos.modules.finance.vendor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {

    Optional<VendorBill> findByIdAndCompanyId(Long id, Long companyId);

    Page<VendorBill> findByCompanyId(Long companyId, Pageable pageable);

    Page<VendorBill> findByCompanyIdAndStatus(Long companyId, VendorBillStatus status, Pageable pageable);

    Page<VendorBill> findByCompanyIdAndVendorId(Long companyId, Long vendorId, Pageable pageable);

    boolean existsByVendorIdAndCompanyId(Long vendorId, Long companyId);

    /** APPROVED/PARTIALLY_PAID bills = money the company still owes. */
    @Query("SELECT b FROM VendorBill b WHERE b.companyId = :companyId AND b.status IN :statuses")
    List<VendorBill> findOutstandingByCompanyId(@Param("companyId") Long companyId,
                                                 @Param("statuses") List<VendorBillStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.balanceAmount), 0) FROM VendorBill b " +
           "WHERE b.companyId = :companyId AND b.vendor.id = :vendorId " +
           "AND b.status IN (com.businessos.modules.finance.vendor.VendorBillStatus.APPROVED, " +
           "com.businessos.modules.finance.vendor.VendorBillStatus.PARTIALLY_PAID)")
    BigDecimal sumOutstandingByVendor(@Param("companyId") Long companyId, @Param("vendorId") Long vendorId);

    @Query("SELECT MAX(b.billNumber) FROM VendorBill b WHERE b.companyId = :companyId AND b.billNumber LIKE :prefix%")
    Optional<String> findMaxBillNumberByCompanyAndPrefix(@Param("companyId") Long companyId, @Param("prefix") String prefix);
}
