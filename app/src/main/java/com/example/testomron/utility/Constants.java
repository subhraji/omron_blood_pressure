package com.example.testomron.utility;

/**
 * Created by Omron HealthCare Inc
 */

public interface Constants {
    String USER_EMAIl = "useremail@domain.com";
    int CONNECTION_TIMEOUT = 15;

    public interface extraKeys {
        String KEY_SELECTED_DEVICE = "selectedDevice";
        String KEY_PERSONAL_SETTINGS = "personalSettings";
        String KEY_DEVICE_LOCAL_NAME = "localName";
        String KEY_WEIGHT_SETTINGS = "weightSettings";
        String KEY_SELECTED_USER = "selectedUser";
    }

    public interface bundleKeys {
        String KEY_BUNDLE_HEIGHT_CM = "height_cm";
        String KEY_BUNDLE_STRIDE_CM = "stride_cm";
        String KEY_BUNDLE_WEIGHT_KG = "weight_kg";
        String KEY_ACTIVITY_DATA_KEY="acivity_data_key";
        String KEY_ACTIVITY_DATA_TYPE="acivity_data_type";
        String KEY_ACTIVITY_DATA_SEQ="acivity_data_seq";
        String KEY_ACTIVITY_DATA_DATE="acivity_data_date";
//        String KEY_BUNDLE_SELECTED_USER = "selectedUser";
        String KEY_BUNDLE_DOB = "dob";
        String KEY_BUNDLE_GENDER = "gender";
        String KEY_BUNDLE_WEIGHT_UNIT = "weight_unit";
        String KEY_BUNDLE_IS_WEIGHT_UPDATE = "weight_update";



    }

}
