package hooks;

import config.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class hook {
    @Before
    public void setup() {
        DriverFactory.initDriver();
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }

}

