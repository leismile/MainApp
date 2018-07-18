package com.ubtechinc.alpha.network.module;

import android.support.annotation.Keep;

import com.ubtechinc.nets.http.Url;

import java.io.Serializable;
import java.util.List;

/**
 * @Date: 2017/10/27.
 * @Author: Liu Dongyang
 * @Modifier :
 * @Modify Date:
 * [A brief description] :
 */
@Keep
public class CheckBindRobotModule {

    @Url("/alpha2-web/relation/getBindUsers")
    @Keep
    public static class Request {
        private String robotUserId;

        public Request(String robotUserId) {
            this.robotUserId = robotUserId;
        }

        public String getRobotUserId() {
            return robotUserId;
        }

        public void setRobotUserId(String robotUserId) {
            this.robotUserId = robotUserId;
        }
    }

    @Keep
    public class Response extends BaseResponse {

        private Data data;

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "data=" + data +
                    '}';
        }
    }

    @Keep
    public class Data {
        private List<User> result;

        public List<User> getResult() {
            return result;
        }

        public void setResult(List<User> result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "result=" + result +
                    '}';
        }
    }
    @Keep
    public class User implements Serializable{


        private String userImage;

        private String userName;

        private String nickName;

        private int userId;

        private int upUser;

        private String relationDate;

        public String getRelationDate() {
            return relationDate;
        }

        public void setRelationDate(String relationDate) {
            this.relationDate = relationDate;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }
        public int getUpUser() {
            return upUser;
        }

        public void setUpUser(int upUser) {
            this.upUser = upUser;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "User{" +
                    "userImage='" + userImage + '\'' +
                    ", userName='" + userName + '\'' +
                    ", nickName='" + nickName + '\'' +
                    ", userId=" + userId +
                    ", upUser=" + upUser +
                    '}';
        }
    }
}
