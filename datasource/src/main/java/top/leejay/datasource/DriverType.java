package top.leejay.datasource;

public enum DriverType {
    MYSQL("com.mysql.cj.jdbc.Driver");

    private String driver;

    DriverType(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
}
