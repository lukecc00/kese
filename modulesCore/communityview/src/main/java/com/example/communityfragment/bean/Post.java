package com.example.communityfragment.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private int id;
    private String userid;
    private String userName;
    private String userAvatar;
    private String content;
    private String createdTime;
    private String imageUrls;
    private String likeConunt;
    private boolean isLiked;
    private String commentCount;
    private String communityId;

    public Post() {
    }

    public Post(String content, String likeConunt, String userAvatar, String userid) {
        this.content = content;
        this.likeConunt = likeConunt;
        this.userAvatar = userAvatar;
        this.userid = userid;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getLikeConunt() {
        return likeConunt;
    }

    public void setLikeConunt(String likeConunt) {
        this.likeConunt = likeConunt;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @Override
    public String toString() {
        return "Post{" +
                "commentCount='" + commentCount + '\'' +
                ", communityId='" + communityId + '\'' +
                ", isLiked=" + isLiked +
                ", content='" + content + '\'' +
//                ", createdTime='" + createdTime + '\'' +
                ", id=" + id +
//                ", imageUrls='" + imageUrls + '\'' +
                ", likeConunt='" + likeConunt + '\'' +
//                ", userAvatar='" + userAvatar + '\'' +
                ", userid='" + userid + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    public static List<Post> getPostList() {
        List<Post> postList = new ArrayList<>();

        // 第一条模拟数据
        Post post1 = new Post();
        post1.setId(1);
        post1.setUserid("user_001");
        post1.setUserName("小明");
        post1.setUserAvatar("https://example.com/avatar1.png"); // 模拟头像地址
        post1.setContent("今天去公园散步，天气超级好！😀");
        post1.setCreatedTime("2025-12-24 10:30:00");
        post1.setImageUrls("https://example.com/img1.jpg"); // 模拟图片地址
        post1.setLikeConunt("28");
        post1.setLiked(true); // 已点赞
        post1.setCommentCount("8");
        post1.setCommunityId("community_001");

        // 第二条模拟数据
        Post post2 = new Post();
        post2.setId(2);
        post2.setUserid("user_002");
        post2.setUserName("小红");
        post2.setUserAvatar("https://example.com/avatar2.png");
        post2.setContent("分享一款超好用的APP，推荐给大家～");
        post2.setCreatedTime("2025-12-25 09:15:00");
        post2.setImageUrls("https://example.com/img2.jpg,https://example.com/img3.jpg"); // 多个图片用逗号分隔
        post2.setLikeConunt("56");
        post2.setLiked(false); // 未点赞
        post2.setCommentCount("15");
        post2.setCommunityId("community_001");

        // 将两条数据加入列表
        postList.add(post1);
        postList.add(post2);

        return postList;
    }
}
