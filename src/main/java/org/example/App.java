package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpsParameters;


import javax.swing.text.html.HTML;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class App {

    private static final String POST_API_URL = "http://jsonplaceholder.typicode.com/";
    private static ObjectMapper mapper = new ObjectMapper(); //  convert the json into java class

    //create a function which get a url parameter and create a http response
    public static HttpResponse<String> getHttp (String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder() // Creating a http request
                .GET()
                .header("accept", "application/json")
                .uri(URI.create(POST_API_URL + url))
                .timeout(Duration.ofSeconds(2))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode(); //check status code
        if (status == 200)
            return response;
        else
            System.out.println ("THE CONNECTION IS FAILED  " + status) ;
            return response;




    }

    //create a function that returns the uncompleted tasks of a specific user
    public static UncompletedUser getUserUncompletedTasks (byte userid) throws IOException, InterruptedException {

        HttpResponse<String> response_tasks = getHttp("todos?userId=" + userid + "&completed=false");
        List<Task> tasks = mapper.readValue(response_tasks.body(), new TypeReference<List<Task>>() {}); // creating a list which contains the response_tasks value
        UncompletedUser uncompleteduser = new UncompletedUser(); //creating a new object that contains userid with uncompleted tasks list
        uncompleteduser.setUserId(userid);
        uncompleteduser.setTasks(tasks);
        return uncompleteduser;
    }

    //create a function that returns the uncompleted tasks of all users
    public static PrimaryUsersList usersUncompletedTasks () throws IOException, InterruptedException {

        PrimaryUsersList primaryuncomletedusers = new PrimaryUsersList();
        List<UncompletedUser> listusers = new ArrayList<UncompletedUser>();
        HttpResponse<String> response_users = getHttp("users");
        List<User> users = mapper.readValue(response_users.body(), new TypeReference<List<User>>() {});

        for( User user : users){  // running on all the users and update it on the list of UncompletedUser users
            UncompletedUser uncompleteduser = getUserUncompletedTasks(user.getId());
            listusers.add(uncompleteduser);
        };

        primaryuncomletedusers.setUsers(listusers); // insert the list into a one object
        return primaryuncomletedusers;
    }

    //create a function that returns all the comments of a specific post
    public static List<Comment> getComments (short postId) throws IOException, InterruptedException {
        HttpResponse<String> response_comments = getHttp("comments?postId=" + postId);
        List<Comment> comments = mapper.readValue(response_comments.body(), new TypeReference<List<Comment>>() {});
        return comments;
    }

    //create a function that returns all the posts of a specific user
    public static List<Post> get_Posts (byte userId) throws IOException, InterruptedException {
        HttpResponse<String> response_posts = getHttp("posts?userId=" + userId);
        List<Post> posts = mapper.readValue(response_posts.body(), new TypeReference<List<Post>>() {});
        return posts;
    }

    //create a primary function that returns all emails per post per user.
    public static userPostReplierEmails getPostsReplierEmails () throws IOException, InterruptedException {
        userPostReplierEmails userpostReplierEmails = new userPostReplierEmails();
        List<userPosts> listusers = new ArrayList<userPosts>();
        HttpResponse<String> response_users = getHttp("users");
        List<User> users = mapper.readValue(response_users.body(), new TypeReference<List<User>>() {});

        //running on all the users and creating each of them list of their posts
        for( User user : users){
            userPosts userposts = new userPosts();
            List<Post> posts = get_Posts(user.getId());
            List<PostEmails> postEmailslist = new ArrayList<PostEmails>();

            //running on all the posts and checking each of them. if there are comments, update the post id and his comments on thr postEmail object.
            for( Post post : posts) {
                Thread.sleep(100);
                List<Comment> comments = getComments((short) post.getId());
                if (comments.isEmpty() == false) {
                    PostEmails postemails = new PostEmails();
                    postemails.setEmails(comments);
                    postemails.setPostid(post.getId());
                    postEmailslist.add(postemails);
                }
            }

            userposts.setPosts(postEmailslist);
            userposts.setUserId(user.getId());

            listusers.add(userposts);// after the userpost object is done we add it to the primary list of users
        };
        userpostReplierEmails.setUsersPosts(listusers);
        return userpostReplierEmails;
    }


    //create a function that returns all albums of a specific user that contains more photos than a given threshold
    public static AlbumsPerUser getAlbums (byte userId, short photo_input) throws IOException, InterruptedException {
        HttpResponse<String> response_comments = getHttp("albums?userId=" + userId);
        List<albums> Albums = mapper.readValue(response_comments.body(), new TypeReference<List<albums>>() {});
        List<albums> new_album = new ArrayList<albums>();
        AlbumsPerUser albumsPerUser1 = new AlbumsPerUser();
        List<AlbumsPerUser> albumsPerUsers = new ArrayList<AlbumsPerUser>();

        //running on all the albums and check if there are more photos then the photo threshold input. if its above the threshold , update the album on the list.
        for( albums album : Albums){
            List<photos> photos = getPhotos(album.getId());

            if (photos.size() >= photo_input) {
            new_album.add(album);
            }
            albumsPerUser1.setUserId(userId);
            albumsPerUser1.setAlbums(new_album);

        };
        return albumsPerUser1;
    }

    //create a function that returns the photos of a specific album
    public static List<photos> getPhotos (byte albumId) throws IOException, InterruptedException {

        HttpResponse<String> response_photos = getHttp("photos?albumId=" + albumId);
        List<photos> photos = mapper.readValue(response_photos.body(), new TypeReference<List<photos>>() {});
        return photos;
    }


    public static void main (String[]args ) throws IOException, InterruptedException {

        //calling the function to get the collection of users with uncompleted tasks

        //PrimaryUsersList primary = usersUncompletedTasks();
        //System.out.println(primary);

        //calling the function to get the collection of  a specific user - id 1 with uncompleted tasks

        /*UncompletedUser user = getUserUncompletedTasks((byte) 2);
        System.out.println(user);*/


        //calling the function that returns the summary for each user, the email of each replier (in a comment) per each post that the user has posted and covert the output into jason

        /*userPostReplierEmails userpostemials = getPostsReplierEmails();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(userpostemials);
        System.out.println(json);*/

        // calling the function that returns all albums of a specific user that contains more photos than a given threshold. convert into jason output

        /*Scanner myObj = new Scanner(System.in);
        System.out.println("Enter the minimum number of photos (threshold): ");
        short photo_input = myObj.nextShort();
        AlbumsPerUser albumsPerUser = getAlbums((byte) 2, photo_input);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(albumsPerUser);
        System.out.println(json);*/





    }



}
