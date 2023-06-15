/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import model.User;
import database.SQLHandler;
import java.util.List;
/**
 *
 * @author boi09
 */
public class Client implements Runnable{
    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;
    private String clientIP;
    private User user;
    private SQLHandler sqlhandler;
    private Room room;

   public BufferedReader getIs() {
        return is;
    }
    
    public BufferedWriter getOs() {
        return os;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
    
    public int getClientNumber() {
        return clientNumber;
    }

    public User getUser() {
        return user;
    }


    public Room getRoom() {
        return room;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Client(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
        System.out.println("Server thread number " + clientNumber + " Started");
        sqlhandler = new SQLHandler();
        isClosed = false;
        room = null;
        //Trường hợp test máy ở server sẽ lỗi do hostaddress là localhost
        if(this.socketOfServer.getInetAddress().getHostAddress().equals("127.0.0.1")){
            clientIP = "127.0.0.1";
        }
        else{
            clientIP = this.socketOfServer.getInetAddress().getHostAddress();
        }
    }

    @Override
    public void run() {
        try {
            // Mở luồng vào ra trên Socket tại Server.
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            System.out.println("Khời động luông mới thành công, ID là: " + clientNumber);
            write("server-send-id" + "," + this.clientNumber);
            String message;
           while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                String[] messageSplit = message.split(",");
                //Xác minh
                if(messageSplit[0].equals("client-verify")){ //mess1 tài khoản mess2 pass
                    System.out.println(message);
                    User user1 = sqlhandler.verifyUser(new User(messageSplit[1], messageSplit[2]));
                    if(user1==null)
                        write("wrong-user,"+messageSplit[1]+","+messageSplit[2]); 
                    else if(!user1.getIsOnline()&&!sqlhandler.checkIsBanned(user1)){
                        write("login-success,"+getStringFromUser(user1));
                        this.user = user1;
                        sqlhandler.updateToOnline(this.user.getID()); // trả về đã online
                        RunServer.clientmanager.boardCast(clientNumber, "chat-server,"+user1.getNickname()+" đang online");
                        RunServer.host.addMessage("["+user1.getID()+"] "+user1.getNickname()+ " đang online");
                    } else if(!sqlhandler.checkIsBanned(user1)){
                        write("dupplicate-login,"+messageSplit[1]+","+messageSplit[2]);
                    }
                    else{
                        write("banned-user,"+messageSplit[1]+","+messageSplit[2]);
                    }
                }
                //Xác minh và thay đổi mật khẩu
                if(messageSplit[0].equals("vpass")){ 
                    System.out.println(message);
                    int idofuser = Integer.parseInt(messageSplit[1]);
                    boolean pass1 = sqlhandler.checkDupPass(idofuser , messageSplit[2]);
                    if(pass1==true)
                    {
                        User changepass = new User(messageSplit[3],idofuser );
                        sqlhandler.chagnePass(changepass);
                    }
                    else{
                        write("duppassword,"+messageSplit[2]+","+messageSplit[3]);
                    }
                }
                // Đăng kí 
                if(messageSplit[0].equals("register")){
                   boolean checkdup = sqlhandler.checkDuplicated(messageSplit[1]);
                   if(checkdup) write("duplicate-username,");
                   else{
                       User userRegister = new User(messageSplit[1], messageSplit[2], messageSplit[3], messageSplit[4]);
                       sqlhandler.addUser(userRegister);
                       User userRegistered = sqlhandler.verifyUser(userRegister);
                       this.user = userRegistered;
                       sqlhandler.updateToOnline(this.user.getID());
                       RunServer.clientmanager.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đang online");
                       write("login-success,"+getStringFromUser(this.user));
                   }
                }
                //Chỉnh sửa profile
                if(messageSplit[0].equals("change_profile")){
                    int idofuser = Integer.parseInt(messageSplit[1]);
                    User changeprofile = new User(idofuser, messageSplit[2], messageSplit[3]);
                    this.user.setAvatar(messageSplit[3]);
                    this.user.setNickname(messageSplit[2]);
                    sqlhandler.chagnePro(changeprofile);  
                }

                //sắp xếp rank
                if(messageSplit[0].equals("get-rank-charts")){
                    List<User> ranks = sqlhandler.getUserStaticRank();
                    String res = "return-get-rank-charts,";
                    for(User user : ranks){
                        res += getStringFromUser(user)+",";
                    }
                    System.out.println(res);
                    write(res);
                }
                //sửa thông tin
//                if(messageSplit[0].equals("change_profile")){
//                    sqlhandler.changeprofile(messageSplit[1],this.user.getID());
//                }
                //Xử lý xem danh sách bạn bè
                if(messageSplit[0].equals("view-friend-list")){
                    List<User> friends = sqlhandler.getListFriend(this.user.getID());
                    String res = "return-friend-list,";
                    for(User friend : friends){
                        res += friend.getID() + "," + friend.getNickname()+"," + (friend.getIsOnline()==true?1:0) +"," + (friend.getIsPlaying()==true?1:0)+",";
                    }
                    System.out.println(res);
                    write(res);
                }
                //Xử lý phòng trống
                if (messageSplit[0].equals("view-room-list")) {
                    String res = "room-list,";
                    int number = 1;
                    for (Client client : RunServer.clientmanager.getListServerThreads()) {
                        if(number>8) break;
                        if (client.room != null && client.room.getNumberOfUser() == 1) {
                            res += client.room.getID() + "," +client.room.getUser1().getUser().getNickname()+"," + client.room.getPassword() + ",";
                        }
                        number++;
                    }
                    write(res);
                    System.out.println(res);
                }
               //xử lý xem danh sách client
                if (messageSplit[0].equals("view-user")) {
                    String res = "user-list,";
                    String room = "";
                    for (Client client : RunServer.clientmanager.getListServerThreads()) {
//                        if (client.getRoom() == null) {
//                            room = null;
//                        } else {
//                            room = "" + client.getRoom().getID();
//                        }
//                        if (client.getUser() != null) {
//                            res +="User: " + client.getUser().getNickname() + " Room: " + room+",";
//                        }
                        if (client.getUser() != null){
                            res += getStringFromUser(client.getUser())+",";
                        }    
                    }
                    write(res);
                    System.out.println(res);
                }
                 //Xử lý yêu cầu kết bạn
                if (messageSplit[0].equals("make-friend")){
                    RunServer.clientmanager.getServerThreadByUserID(Integer.parseInt(messageSplit[1]))
                            .write("make-friend-request,"+this.user.getID()+","+sqlhandler.getNickNameByID(this.user.getID()));
                }
                //Xử lý lấy thông tin kết bạn và rank
                if(messageSplit[0].equals("check-friend")){
                    String res = "check-friend-response,";
                    res += (sqlhandler.checkIsFriend(this.user.getID(), Integer.parseInt(messageSplit[1]))?1:0);
                    write(res);
                }
                //Xử lý xác nhận kết bạn
                if(messageSplit[0].equals("make-friend-confirm")){
                    sqlhandler.makeFriend(this.user.getID(), Integer.parseInt(messageSplit[1]));
                    RunServer.clientmanager.getServerThreadByUserID(Integer.parseInt(messageSplit[1]))                           
                    .write("Friend-request-accepted");//trả về lại thằng đã gửi yêu cầu kết bạn rằng bạn đã kết bạn thành công
                }
                //Xử lý chat toàn server
                if(messageSplit[0].equals("chat-server")){
                    RunServer.clientmanager.boardCast(clientNumber,messageSplit[0]+","+ user.getNickname()+" : "+ messageSplit[1]);
                    RunServer.host.addMessage("["+user.getID()+"] "+user.getNickname()+" : "+ messageSplit[1]);
                }
                //Xử lý tạo phòng
                if (messageSplit[0].equals("create-room")) {
                    room = new Room(this);
                    if (messageSplit.length == 2) {
                        room.setPassword(messageSplit[1]);
                        write("your-created-room," + room.getID() + "," + messageSplit[1]);
                        System.out.println("Tạo phòng mới thành công, password là " + messageSplit[1]);
                    } else {
                        write("your-created-room," + room.getID());
                        System.out.println("Tạo phòng mới thành công");
                    } 
                    sqlhandler.updateToPlaying(this.user.getID());
                }
                //Xử lý tìm phòng nhanh
                if (messageSplit[0].equals("quick-room")) {
                    boolean isFinded = false;
                    for (Client client : RunServer.clientmanager.getListServerThreads()) {
                        if (client.room != null && client.room.getNumberOfUser() == 1 && client.room.getPassword().equals(" ")) {
                            //nếu có phòng cho chơi thì
                            sqlhandler.updateToPlaying(this.user.getID()); //update đang tìm trận
                            //setvisible đã tìm thấy trận
                            write("Found-room");                           
                            isFinded = true;
                            break;
                        }
                    }                    
                    if (!isFinded) {
                        this.room = new Room(this);
                        sqlhandler.updateToPlaying(this.user.getID());
                        System.out.println("Không tìm thấy phòng, tạo phòng mới");
                    }
                }                
                if (messageSplit[0].equals("accept-room")) {
                    for (Client client : RunServer.clientmanager.getListServerThreads()) {
                        if (client.room != null && client.room.getNumberOfUser() == 1 && client.room.getPassword().equals(" ")) {
                            client.room.setUser2(this);
                            this.room = client.room;
                            room.increaseNumberOfGame();
                            System.out.println("Đã vào phòng " + room.getID());
                            goToPartnerRoom();
                            sqlhandler.updateToPlaying(this.user.getID());
                            break;
                        }
                    }                    
                }
                if (messageSplit[0].equals("join-room")) {
                    int ID_room = Integer.parseInt(messageSplit[1]);
                    for (Client client : RunServer.clientmanager.getListServerThreads()) {
                        if (client.room != null && client.room.getID() == ID_room) {
                            client.room.setUser2(this);
                            this.room = client.room;
                            System.out.println("Đã vào phòng " + room.getID());
                            room.increaseNumberOfGame();
                            goToPartnerRoom();
                            sqlhandler.updateToPlaying(this.user.getID());
                            break;
                        }
                    }
                }
                if(messageSplit[0].equals("draw-request")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("draw-confirm")){
                    room.increaseNumberOfDraw();
                    room.increaseNumberOfGame();
                    room.boardCast("draw-game,");
                }
                if(messageSplit[0].equals("draw-refuse")){
                    room.getCompetitor(clientNumber).write("draw-refuse,");
                }
                if(messageSplit[0].equals("undo-request")){
                    room.getCompetitor(clientNumber).write(message);
                }
                 if(messageSplit[0].equals("undo-confirm")){
                    room.boardCast("undo-game,");
                }
                if(messageSplit[0].equals("undo-refuse")){
                    room.getCompetitor(clientNumber).write("undo-refuse,");
                }
                //hủy phòng
                if (messageSplit[0].equals("cancel-room")) {
                    sqlhandler.updateToNotPlaying(this.user.getID());
                    System.out.println("Đã hủy phòng");
                    this.room = null;
                }
                 //Xử lý khi người chơi đánh 1 nước
                if(messageSplit[0].equals("caro")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("chat")){
                    room.getCompetitor(clientNumber).write(message);
                }
                if(messageSplit[0].equals("win")){
                    sqlhandler.addWinGame(this.user.getID());
                    room.increaseNumberOfGame();
                    room.getCompetitor(clientNumber).write("caro,"+messageSplit[1]+","+messageSplit[2]);
                    room.boardCast("new-game,");
                }
                if(messageSplit[0].equals("lose")){
                    sqlhandler.addWinGame(room.getCompetitor(clientNumber).user.getID());
                    room.increaseNumberOfGame();
                    room.getCompetitor(clientNumber).write("competitor-time-out");
                    write("new-game,");
                }
                if(messageSplit[0].equals("left-room")){
                    if (room != null) {
                        room.setUsersToNotPlaying();
                        room.decreaseNumberOfGame();
                        room.getCompetitor(clientNumber).write("left-room,");
                        room.getCompetitor(clientNumber).room = null;
                        this.room = null;
                    }
                }
                if(messageSplit[0].equals("offline")){
                    sqlhandler.updateToOffline(this.user.getID());
                    RunServer.host.addMessage("["+user.getID()+"] "+user.getNickname()+" đã offline");
                    RunServer.clientmanager.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đã offline");
                    this.user=null;
                }                
           } 
        } catch (IOException e) {
            //Thay đổi giá trị cờ để thoát luồng
            isClosed = true;
            //Cập nhật trạng thái của user
            if(this.user!=null){
                System.out.println(this.user.getID());
                sqlhandler.updateToOffline(this.user.getID());
                sqlhandler.updateToNotPlaying(this.user.getID());
                RunServer.clientmanager.boardCast(clientNumber, "chat-server,"+this.user.getNickname()+" đã offline");
                RunServer.host.addMessage("["+user.getID()+"] "+user.getNickname()+" đã offline");
            }
            RunServer.clientmanager.remove(clientNumber);
            System.out.println(this.clientNumber+" đã thoát");
        }
    }
    public void goToOwnRoom() throws IOException{
        write("go-to-room," + room.getID()+","+room.getCompetitor(this.getClientNumber()).getClientIP()+",1,"+getStringFromUser(room.getCompetitor(this.getClientNumber()).getUser()));
        room.getCompetitor(this.clientNumber).write("go-to-room," + room.getID()+","+this.clientIP+",0,"+getStringFromUser(user));
    }
    
    public void goToPartnerRoom() throws IOException{
        write("go-to-room," + room.getID()+",0,"+getStringFromUser(room.getCompetitor(this.getClientNumber()).getUser()));
        room.getCompetitor(this.clientNumber).write("go-to-room,"+ room.getID()+",1,"+getStringFromUser(user));
    }
     public String getStringFromUser(User user1){
        return ""+user1.getID()+","+user1.getUsername()
                                +","+user1.getPassword()+","+user1.getNickname()+","+
                                user1.getAvatar()+","+user1.getNumberOfGame()+","+
                                user1.getNumberOfwin()+","+user1.getNumberOfDraw()+","+user1.getRank();
    }
    public void write(String message) throws IOException{
        os.write(message);
        os.newLine();
        os.flush();
    }
}
