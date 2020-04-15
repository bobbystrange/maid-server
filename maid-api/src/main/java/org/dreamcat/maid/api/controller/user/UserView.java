package org.dreamcat.maid.api.controller.user;

import lombok.Data;

/**
 * Create by tuke on 2020/3/14
 */
@Data
public class UserView {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
