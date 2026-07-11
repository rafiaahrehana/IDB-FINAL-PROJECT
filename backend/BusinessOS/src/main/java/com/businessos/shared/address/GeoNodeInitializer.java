package com.businessos.shared.address;

import com.businessos.shared.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class GeoNodeInitializer implements CommandLineRunner {

    private final GeoNodeRepository geoNodeRepository;

    @Override
    public void run(String... args) {
        try {
            if (geoNodeRepository.count() == 0) {
                Map<String, String> countries = new HashMap<>();
                // South Asia
                countries.put("BD", "Bangladesh"); countries.put("IN", "India"); countries.put("PK", "Pakistan"); countries.put("LK", "Sri Lanka"); countries.put("NP", "Nepal"); countries.put("MV", "Maldives"); countries.put("AF", "Afghanistan");
                // Southeast & East Asia
                countries.put("CN", "China"); countries.put("JP", "Japan"); countries.put("KR", "South Korea"); countries.put("ID", "Indonesia"); countries.put("MY", "Malaysia"); countries.put("PH", "Philippines"); countries.put("SG", "Singapore"); countries.put("TH", "Thailand"); countries.put("VN", "Vietnam"); countries.put("TW", "Taiwan"); countries.put("MM", "Myanmar"); countries.put("KH", "Cambodia");
                // Americas
                countries.put("US", "United States"); countries.put("CA", "Canada"); countries.put("MX", "Mexico"); countries.put("BR", "Brazil"); countries.put("AR", "Argentina"); countries.put("CO", "Colombia"); countries.put("CL", "Chile"); countries.put("PE", "Peru"); countries.put("VE", "Venezuela"); countries.put("EC", "Ecuador"); countries.put("CU", "Cuba"); countries.put("BO", "Bolivia"); countries.put("PY", "Paraguay"); countries.put("UY", "Uruguay"); countries.put("CR", "Costa Rica"); countries.put("PA", "Panama"); countries.put("SV", "El Salvador"); countries.put("GT", "Guatemala"); countries.put("HN", "Honduras"); countries.put("DO", "Dominican Republic");
                // Europe
                countries.put("GB", "United Kingdom"); countries.put("DE", "Germany"); countries.put("FR", "France"); countries.put("IT", "Italy"); countries.put("ES", "Spain"); countries.put("RU", "Russia"); countries.put("UA", "Ukraine"); countries.put("PL", "Poland"); countries.put("NL", "Netherlands"); countries.put("BE", "Belgium"); countries.put("SE", "Sweden"); countries.put("CH", "Switzerland"); countries.put("AT", "Austria"); countries.put("PT", "Portugal"); countries.put("GR", "Greece"); countries.put("CZ", "Czech Republic"); countries.put("RO", "Romania"); countries.put("HU", "Hungary"); countries.put("IE", "Ireland"); countries.put("DK", "Denmark"); countries.put("FI", "Finland"); countries.put("NO", "Norway"); countries.put("TR", "Turkey"); countries.put("BY", "Belarus"); countries.put("RS", "Serbia"); countries.put("BG", "Bulgaria"); countries.put("SK", "Slovakia"); countries.put("HR", "Croatia"); countries.put("LT", "Lithuania"); countries.put("LV", "Latvia");
                // Middle East & North Africa
                countries.put("AE", "United Arab Emirates"); countries.put("SA", "Saudi Arabia"); countries.put("EG", "Egypt"); countries.put("IR", "Iran"); countries.put("IL", "Israel"); countries.put("IQ", "Iraq"); countries.put("DZ", "Algeria"); countries.put("MA", "Morocco"); countries.put("QA", "Qatar"); countries.put("KW", "Kuwait"); countries.put("OM", "Oman"); countries.put("LB", "Lebanon"); countries.put("JO", "Jordan"); countries.put("TN", "Tunisia");
                // Sub-Saharan Africa
                countries.put("ZA", "South Africa"); countries.put("NG", "Nigeria"); countries.put("KE", "Kenya"); countries.put("ET", "Ethiopia"); countries.put("GH", "Ghana"); countries.put("TZ", "Tanzania"); countries.put("UG", "Uganda"); countries.put("CI", "Ivory Coast"); countries.put("CM", "Cameroon"); countries.put("AO", "Angola"); countries.put("ZM", "Zambia"); countries.put("ZW", "Zimbabwe"); countries.put("SN", "Senegal"); countries.put("RW", "Rwanda");
                // Oceania
                countries.put("AU", "Australia"); countries.put("NZ", "New Zealand"); countries.put("FJ", "Fiji");

                Map<String, GeoNode> savedCountries = new HashMap<>();

                for (Map.Entry<String, String> entry : countries.entrySet()) {
                    GeoNode cNode = geoNodeRepository.save(GeoNode.builder()
                            .name(entry.getValue())
                            .type(GeoNodeType.COUNTRY)
                            .code(entry.getKey())
                            .build());
                    savedCountries.put(entry.getKey(), cNode);
                }

                // Seed sample hierarchy for Bangladesh
                GeoNode bd = savedCountries.get("BD");
                if (bd != null) {
                    GeoNode dhakaDiv = geoNodeRepository.save(GeoNode.builder().name("Dhaka Division").type(GeoNodeType.LEVEL1).parent(bd).build());
                    GeoNode dhakaDist = geoNodeRepository.save(GeoNode.builder().name("Dhaka District").type(GeoNodeType.LEVEL2).parent(dhakaDiv).build());
                    GeoNode gulshanUpazila = geoNodeRepository.save(GeoNode.builder().name("Gulshan Upazila").type(GeoNodeType.LEVEL3).parent(dhakaDist).build());
                    geoNodeRepository.save(GeoNode.builder().name("Gulshan Police Station").type(GeoNodeType.LEVEL4).parent(gulshanUpazila).build());
                }

                // Seed sample hierarchy for United States
                GeoNode us = savedCountries.get("US");
                if (us != null) {
                    GeoNode nyState = geoNodeRepository.save(GeoNode.builder().name("New York").type(GeoNodeType.LEVEL1).parent(us).build());
                    GeoNode nyCounty = geoNodeRepository.save(GeoNode.builder().name("New York County").type(GeoNodeType.LEVEL2).parent(nyState).build());
                    GeoNode nycCity = geoNodeRepository.save(GeoNode.builder().name("New York City").type(GeoNodeType.LEVEL3).parent(nyCounty).build());
                    geoNodeRepository.save(GeoNode.builder().name("1st Precinct").type(GeoNodeType.LEVEL4).parent(nycCity).build());
                }
            }
        } catch (Exception ex) {
            throw new InternalServerException(
                    "Failed to initialize GeoNode master data.",
                    ex
            );
        }
    }
}
