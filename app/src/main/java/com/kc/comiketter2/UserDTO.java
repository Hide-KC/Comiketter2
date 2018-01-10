package com.kc.comiketter2;

import twitter4j.User;

/**
 * Created by HIDE on 2017/11/26.
 */

public class UserDTO {
    public Long user_id = 0L;
    public String name = "";
    public String screen_name = "";
    public String profile_image_url = "";
    public String profile_description = "";

    public Integer auto_day = 0;
    public Integer manual_day = 0;
    public String circle_name = "";
    public String circle_space = "";
    public Integer hole_id = 0;
    public Integer target = 0;
    public Integer busuu = 0;
    public Integer yosan = 0;
    public String memo = "";
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
