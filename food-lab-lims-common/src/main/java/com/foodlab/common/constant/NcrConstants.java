package com.foodlab.common.constant;

public interface NcrConstants {

    String NCR_STATUS_RECHECK = "recheck";
    String NCR_STATUS_CAUSE_ANALYSIS = "cause_analysis";
    String NCR_STATUS_CORRECTIVE_ACTION = "corrective_action";
    String NCR_STATUS_PREVENTIVE_ACTION = "preventive_action";
    String NCR_STATUS_CLOSED = "closed";
    String NCR_STATUS_CANCELLED = "cancelled";

    String RECHECK_STATUS_PENDING = "pending";
    String RECHECK_STATUS_QUALIFIED = "qualified";
    String RECHECK_STATUS_UNQUALIFIED = "unqualified";

    String ACTION_STATUS_PENDING = "pending";
    String ACTION_STATUS_IN_PROGRESS = "in_progress";
    String ACTION_STATUS_COMPLETED = "completed";
    String ACTION_STATUS_VERIFIED = "verified";

    String NCR_SOURCE_AUTO = "auto";
    String NCR_SOURCE_MANUAL = "manual";

    String CAUSE_TYPE_PERSONNEL = "personnel";
    String CAUSE_TYPE_EQUIPMENT = "equipment";
    String CAUSE_TYPE_MATERIAL = "material";
    String CAUSE_TYPE_METHOD = "method";
    String CAUSE_TYPE_ENVIRONMENT = "environment";
    String CAUSE_TYPE_OTHER = "other";
}
