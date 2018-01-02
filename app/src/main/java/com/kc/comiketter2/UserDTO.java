package com.kc.comiketter2;

import twitter4j.User;

/**
 * Created by HIDE on 2017/11/26.
 */

public class UserDTO {
    public Long user_id = 0L;
    public String name = null;
    public String screen_name = null;
    public String profile_image_url = null;
    public String profile_description = null;

    public Integer auto_day = 0;
    public Integer manual_day = 0;
    public String circle_name = null;
    public String circle_space = null;
    public Integer target = 0;
    public Integer busuu = 0;
    public Integer yosan = 0;
    public String memo = null;
    public Integer pickup = 0;
    public Integer hasgot = 0;

    public UserDTO(User user){
        user_id = user.getId();
        name = user.getName();
        screen_name = user.getScreenName();
        profile_image_url = user.getProfileImageURL();
        profile_description = user.getDescription();
    }

    public UserDTO(){

    }
}
