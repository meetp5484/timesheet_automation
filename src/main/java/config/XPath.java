package config;

import org.checkerframework.checker.index.qual.PolyUpperBound;

public class XPath {

    // Login Page
    public static final String MICROSOFT_BUTTON = "//a[@id='social-ms_keycloak_oidc']";
    public static final String USERNAME_FIELD = "//input[@id='i0116']";
    public static final String PASSWORD_FIELD = "//input[@id='i0118']";
    public static final String SIGNIN_NEXT_BUTTON = "//input[@id='idSIButton9']";
    public static final String NO_SELECT_STAY_SIGNED_IN = "//input[@id='idBtn_Back']";

    // Dashboard Page
    public static final String DASHBOARD_LOGO = "//img[@alt='ComsLogo']";
    public static final String ADD_ROW = "//div[@title='Add a row']//div[@class='dx-button-content']";
    public static final String PROJECT_NAME = "(//input[@role='combobox'])[1]";
    public static final String JOB_NAME = "(//input[@role='combobox'])[2]";
    public static final String WORK_ITEM = "//input[@role='textbox']";
    public static final String DESCRIPTION = "//textarea[@role='textbox']";
    public static final String HOURS = "//td[@aria-colindex='5']//input[@class='dx-texteditor-input']";
    public static final String SAVE = "//a[@title='Save']";
    public static final String UNDO = "//a[@title='Cancel']";
    public static final String SELECTED_DATE = ".dx-calendar-cell.dx-calendar-selected-date";
    public static final String NAVIGATE_PREVIOUS = "//a[@aria-label='chevronleft']//div[@class='dx-button-content']";
    public static final String NAVIGATE_NEXT = "//a[@aria-label='chevronright']//div[@class='dx-button-content']";
    public static final String EXISTING_HOURS = "//b[contains(normalize-space(),'Total Hour(s):')]";

    // Dropdown
    public static final String DROPDOWN_OPTION = "//div[@class='dx-scrollable-content']//div[@role='option']//div[@title]";

    // Popup
    public static final String POPUP_TEXT = "//h1[contains(text(),'Your attendance')]";
    public static final String POPUP_CANCEL = "//span[normalize-space()='Cancel']";
    public static final String WFH_RADIO = "(//div[@class='dx-radiobutton-icon-dot'])[1]";
    public static final String POPUP_MSG = "//textarea[@placeholder='Please provide details of official travel...']";
    public static final String POPUP_OK = "//div[@aria-label='Ok']";
    public static final String LEAVE_RADIO = "(//div[@class='dx-radiobutton-icon-dot'])[2]";
    public static final String OFFICIAL_TRAVEL_RADIO = "(//div[@class='dx-radiobutton-icon-dot'])[3]";



}