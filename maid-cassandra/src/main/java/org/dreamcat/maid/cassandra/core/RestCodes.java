package org.dreamcat.maid.cassandra.core;

/**
 * Create by tuke on 2020/5/24
 */
public abstract class RestCodes {
    /// 403
    public static final int insufficient_permissions = 1;

    /// list
    public static final int fid_not_found = 1;
    public static final int fid_not_file = 2;
    public static final int fid_not_diretory = 3;
    public static final int excessive_subitems = 4;
    public static final int excessive_items = 5;

    /// ops
    public static final int parent_fid_not_found = 1;
    public static final int name_already_exist = 2;
    public static final int unsupported_root = 3;
    public static final int rename_to_same_name = 4;
    public static final int rename_failed = 5;

    public static final int target_fid_not_found = 4;
    public static final int move_to_same_dir = 5;
    public static final int move_failed = 6;

    public static final int remove_failed = 2;

    /// load
    public static final int sign_or_io_failed = 1;
    public static final int upload_no_available_instances = 2;
    public static final int upload_save_hub_failed = 3;

    public static final int download_no_available_instances = 1;

    public static final int upload_falied = 4;

    /// share
    public static final int sid_not_found = 1;
    public static final int sid_require_password = 2;
    public static final int sid_wrong_password = 3;
    public static final int expired_sid = 4;
    public static final int sid_already_invalid = 5;
    public static final int shared_path_not_found = 6;
    public static final int shared_path_not_file = 7;
    public static final int shared_path_not_diretory = 8;
    public static final int sid_excessive_subitems = 9;

    /// user
    public static final int user_not_found = 1;
    public static final int avatar_not_found = 1;

}
