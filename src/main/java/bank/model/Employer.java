package bank.model;

/** Where a Cheque account holder works. */
public class Employer {

    private final String companyName;
    private final String companyAddress;

    public Employer(String companyName, String companyAddress) {
        if (companyName == null || companyName.isBlank()
                || companyAddress == null || companyAddress.isBlank()) {
            throw new IllegalArgumentException("Employer name and address are required");
        }
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
}