package org.dreamcat.maid.cassandra.core;

/**
 * Create by tuke on 2020/5/24
 */
public abstract class RestCodes {
    /// 403
    public static final int insufficient_permissions = 1;
    public static final int insufficient_permissions_for_target_file = 2;

    /// list
    public static final int fid_not_found = 1;
    public static final int fid_not_diretory = 2;
    public static final int excessive_subitems = 3;
    public static final int excessive_items = 4;

    public static final int fid_not_file = 2;

    /// ops
    public static final int name_already_exist = 2;
    public static final int mkdir_operation_failed = 3;
    public static final int unsupported_root = 3;

    public static final int rename_to_same_name = 4;
    public static final int rename_operation_failed = 5;

    public static final int unsupported_operation_root = 2;
    public static final int target_fid_not_found = 3;
    public static final int unsupported_same_dir = 4;
    public static final int move_name_already_exist = 5;
    public static final int move_operation_failed = 6;
    public static final int copy_operation_failed = 5;


    /// load
    public static final int upload_falied = 3;

    /// share
    public static final int sid_not_found = 1;
    public static final int sid_require_password = 2;
    public static final int sid_wrong_password = 3;
    public static final int expired_sid = 4;
    public static final int sid_already_invalid = 5;
    public static final int shared_file_not_found = 6;
    public static final int shared_file_not_shared = 7;
    public static final int shared_file_not_file = 8;
    public static final int shared_file_not_diretory = 8;
    public static final int sid_excessive_subitems = 9;

    /// user
    public static final int user_not_found = 1;
    public static final int avatar_not_found = 1;

}
