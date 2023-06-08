package com.example.testomron.database;

import android.net.Uri;

/**
 * Created by Omron HealthCare Inc
 */

public class OmronDBConstans {

    public static final String AUTHORITY = "com.omronhealthcare.OmronConnectivitySample.Database.OmronDBProvider";
    public static final String DB_NAME = "omron_app.db";
    public static final int DB_VERSION = 1;

    public static final String DEVICE_SELECTED_USER = "selected_user";
    public static final String DEVICE_LOCAL_NAME = "local_name";
    public static final String DEVICE_DISPLAY_NAME = "display_name";
    public static final String DEVICE_IDENTITY_NAME = "identity_name";
    public static final String DEVICE_CATEGORY = "device_category";

    // Blood Pressure Keys
    public static final int VITAL_DATA = 1;

    public static final String VITAL_DATA_TABLE = "vital_data_table";
    public static final String VITAL_DATA_INDEX = "_id";

    public static final String VITAL_DATA_OMRONVitalDataArtifactDetectionKey = "OMRONVitalDataArtifactDetectionKey";
    public static final String VITAL_DATA_OMRONVitalDataCuffFlagKey = "OMRONVitalDataCuffFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataDiastolicKey = "OMRONVitalDataDiastolicKey";
    public static final String VITAL_DATA_OMRONVitalDataIHBDetectionKey = "OMRONVitalDataIHBDetectionKey";
    public static final String VITAL_DATA_OMRONVitalDataIrregularFlagKey = "OMRONVitalDataIrregularFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementDateKey = "OMRONVitalDataMeasurementDateKey";
    public static final String VITAL_DATA_OMRONVitalDataMovementFlagKey = "OMRONVitalDataMovementFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataPulseKey = "OMRONVitalDataPulseKey";
    public static final String VITAL_DATA_OMRONVitalDataSystolicKey = "OMRONVitalDataSystolicKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey = "OMRONVitalDataMeasurementDateUTCKey";

    public static final String VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey = "OMRONVitalDataAtrialFibrillationDetectionFlagKey";
    public static final String VITAL_DATA_OMRONVitalDataDisplayedErrorCodeNightModeKey = "OMRONVitalDataDisplayedErrorCodeNightModeKey";
    public static final String VITAL_DATA_OMRONVitalDataMeasurementModeKey = "OMRONVitalDataMeasurementModeKey";

    public static final Uri VITAL_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + VITAL_DATA_TABLE);

    public static final String SQL_CREATE_VITAL_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text)",
                    VITAL_DATA_TABLE,
                    VITAL_DATA_INDEX,
                    VITAL_DATA_OMRONVitalDataArtifactDetectionKey, VITAL_DATA_OMRONVitalDataCuffFlagKey, VITAL_DATA_OMRONVitalDataDiastolicKey,
                    VITAL_DATA_OMRONVitalDataIHBDetectionKey, VITAL_DATA_OMRONVitalDataIrregularFlagKey, VITAL_DATA_OMRONVitalDataMeasurementDateKey,
                    VITAL_DATA_OMRONVitalDataMovementFlagKey, VITAL_DATA_OMRONVitalDataPulseKey, VITAL_DATA_OMRONVitalDataSystolicKey,
                    VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey, VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey, VITAL_DATA_OMRONVitalDataDisplayedErrorCodeNightModeKey,
                    VITAL_DATA_OMRONVitalDataMeasurementModeKey, DEVICE_SELECTED_USER, DEVICE_LOCAL_NAME,
                    DEVICE_DISPLAY_NAME, DEVICE_IDENTITY_NAME, DEVICE_CATEGORY);


    // Sleep Keys
    public static final int SLEEP_DATA = 2;
    public static final String SLEEP_DATA_TABLE = "sleep_data_table";
    public static final String SLEEP_DATA_INDEX = "_id";

    public static final String SLEEP_DATA_SleepStartTimeKey = "SleepStartTimeKey";
    public static final String SLEEP_DATA_SleepOnSetTimeKey = "SleepOnSetTimeKey";
    public static final String SLEEP_DATA_WakeUpTimeKey = "WakeUpTimeKey";
    public static final String SLEEP_DATA_SleepingTimeKey = "SleepingTimeKey";
    public static final String SLEEP_DATA_SleepEfficiencyKey = "SleepEfficiencyKey";
    public static final String SLEEP_DATA_SleepArousalTimeKey = "SleepArousalTimeKey";
    public static final String SLEEP_DATA_SleepBodyMovementKey = "SleepBodyMovementKey";
    public static final String SLEEP_DATA_StartDateUTCKey = "StartDateUTCKey";
    public static final String SLEEP_DATA_StartEndDateUTCKey = "StartEndDateUTCKey";

    public static final Uri SLEEP_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SLEEP_DATA_TABLE);

    public static final String SQL_CREATE_SLEEP_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement,"
                            + "%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text)",
                    SLEEP_DATA_TABLE,
                    SLEEP_DATA_INDEX,
                    SLEEP_DATA_SleepStartTimeKey, SLEEP_DATA_SleepOnSetTimeKey,
                    SLEEP_DATA_WakeUpTimeKey, SLEEP_DATA_SleepingTimeKey,
                    SLEEP_DATA_SleepEfficiencyKey, SLEEP_DATA_SleepArousalTimeKey,
                    SLEEP_DATA_SleepBodyMovementKey,SLEEP_DATA_StartDateUTCKey,
                    SLEEP_DATA_StartEndDateUTCKey, DEVICE_LOCAL_NAME,
                    DEVICE_DISPLAY_NAME, DEVICE_IDENTITY_NAME,
                    DEVICE_CATEGORY);

    // Record Keys
    public static final int RECORD_DATA = 3;
    public static final String RECORD_DATA_TABLE = "record_data_table";
    public static final String RECORD_DATA_INDEX = "_id";

    public static final String RECORD_DATA_StartDateUTCKey = "StartDateUTCKey";

    public static final Uri RECORD_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + RECORD_DATA_TABLE);

    public static final String SQL_CREATE_RECORD_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,%s text,%s text)",
                    RECORD_DATA_TABLE,
                    RECORD_DATA_INDEX,
                    RECORD_DATA_StartDateUTCKey, DEVICE_LOCAL_NAME, DEVICE_DISPLAY_NAME, DEVICE_IDENTITY_NAME, DEVICE_CATEGORY);

    // Activity Keys
    public static final int ACTIVITY_DATA = 4;

    public static final String ACTIVITY_DATA_TABLE = "activity_data_table";
    public static final String ACTIVITY_DATA_INDEX = "_id";

    public static final String ACTIVITY_DATA_StartDateUTCKey = "StartDateUTCKey";
    public static final String ACTIVITY_DATA_EndDateUTCKey = "EndDateUTCKey";
    public static final String ACTIVITY_DATA_MeasurementValueKey = "MeasurementValueKey";
    public static final String ACTIVITY_DATA_SeqNumKey = "SeqNumKey";
    public static final String ACTIVITY_DATA_Type = "Datatype";

    public static final Uri ACTIVITY_DATA_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ACTIVITY_DATA_TABLE);

    public static final String SQL_CREATE_ACTIVITY_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text)",
                    ACTIVITY_DATA_TABLE,
                    ACTIVITY_DATA_INDEX,
                    ACTIVITY_DATA_StartDateUTCKey, ACTIVITY_DATA_EndDateUTCKey, ACTIVITY_DATA_MeasurementValueKey,
                    ACTIVITY_DATA_SeqNumKey, ACTIVITY_DATA_Type, DEVICE_LOCAL_NAME,
                    DEVICE_DISPLAY_NAME, DEVICE_IDENTITY_NAME, DEVICE_CATEGORY);

    public static final int ACTIVITY_DIVIDED_DATA = 6;

    public static final String ACTIVITY_DIVIDED_DATA_TABLE = "activity_divided_data_table";
    public static final String ACTIVITY_DIVIDED_DATA_INDEX = "_id";

    public static final String ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey = "MainStartDateUTCKey";
    public static final String ACTIVITY_DIVIDED_DATA_StartDateUTCKey = "StartDateUTCKey";
    public static final String ACTIVITY_DIVIDED_DATA_MeasurementValueKey = "MeasurementValueKey";
    public static final String ACTIVITY_DIVIDED_DATA_SeqNumKey = "SeqNumKey";
    public static final String ACTIVITY_DIVIDED_DATA_Type = "type";


    public static final Uri ACTIVITY_DIVIDED_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ACTIVITY_DIVIDED_DATA_TABLE);

    public static final String SQL_CREATE_ACTIVITY_DIVIDED_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,%s text,"
                            + "%s text,%s text)",
                    ACTIVITY_DIVIDED_DATA_TABLE,
                    ACTIVITY_DIVIDED_DATA_INDEX,
                    ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey,
                    ACTIVITY_DIVIDED_DATA_StartDateUTCKey, ACTIVITY_DIVIDED_DATA_MeasurementValueKey, ACTIVITY_DIVIDED_DATA_SeqNumKey,
                    ACTIVITY_DIVIDED_DATA_Type, DEVICE_LOCAL_NAME);


    public static final int REMINDER_DATA = 5;
    public static final String REMINDER_DATA_TABLE = "reminder_data_table";
    public static final String REMINDER_DATA_INDEX = "_id";

    public static final String REMINDER_DATA_DEVICE_LOCAL_NAME = "device_local_name";
    public static final String REMINDER_DATA_TimeFormat = "time_format";
    public static final String REMINDER_DATA_Hour = "time_hour";
    public static final String REMINDER_DATA_Minute = "time_minute";
    public static final String REMINDER_DATA_Days = "time_days";

    public static final Uri REMINDER_DATA_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + REMINDER_DATA_TABLE);

    public static final String SQL_CREATE_REMINDER_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, %s text,%s text,"
                            + "%s text,%s text,%s text)",
                    REMINDER_DATA_TABLE,
                    REMINDER_DATA_INDEX, REMINDER_DATA_DEVICE_LOCAL_NAME, REMINDER_DATA_TimeFormat,
                    REMINDER_DATA_Hour, REMINDER_DATA_Minute, REMINDER_DATA_Days);


    // Weight Data
    public static final String WEIGHT_DATA_StartTimeKey = "WeightStartTimeKey";
    public static final String WEIGHT_DATA_WeightKey = "WeightWeightKey";
    public static final String WEIGHT_DATA_BMIKey = "WeightBMIKey";
    public static final String WEIGHT_DATA_RestingMetabolismKey = "WeightRestingMetabolismKey";


    public static final int WEIGHT_DATA = 7;
    public static final String WEIGHT_DATA_TABLE = "weight_data_table";
    public static final String WEIGHT_DATA_INDEX = "_id";


    public static final Uri WEIGHT_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + WEIGHT_DATA_TABLE);

    public static final String SQL_CREATE_WEIGHT_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,%s text)",
                    WEIGHT_DATA_TABLE,
                    WEIGHT_DATA_INDEX,
                    WEIGHT_DATA_StartTimeKey, WEIGHT_DATA_WeightKey, WEIGHT_DATA_BMIKey,
                    WEIGHT_DATA_RestingMetabolismKey, DEVICE_SELECTED_USER, DEVICE_LOCAL_NAME,
                    DEVICE_DISPLAY_NAME, DEVICE_IDENTITY_NAME, DEVICE_CATEGORY);

    // Pulse Oxymeter Data
    public static final int OXYMETER_DATA = 8;
    public static final String OXYMETER_DATA_TABLE = "oxymeter_data_table";
    public static final String OXYMETER_DATA_INDEX = "_id";

    public static final String OXYMETER_DATA_StartTimeKey = "OxymeterStartTimeKey";
    public static final String OXYMETER_DATA_SpO2Key = "OxymeterSpO2Key";
    public static final String OXYMETER_DATA_PulseKey = "OxymeterPulseKey";
    
    public static final Uri OXYMETER_DATA_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + OXYMETER_DATA_TABLE);

    public static final String SQL_CREATE_OXYMETER_DATA_TABLE = String
            .format("create table %s"
                            + "(%s integer primary key autoincrement, "
                            + "%s text,%s text,%s text,"
                            + "%s text,%s text,"
                            + "%s text,%s text)",
                    OXYMETER_DATA_TABLE,
                    OXYMETER_DATA_INDEX,
                    OXYMETER_DATA_StartTimeKey, OXYMETER_DATA_SpO2Key, OXYMETER_DATA_PulseKey,
                    DEVICE_LOCAL_NAME, DEVICE_DISPLAY_NAME,
                    DEVICE_IDENTITY_NAME, DEVICE_CATEGORY);
}
