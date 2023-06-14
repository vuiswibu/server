/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import database.SQLHandler;
import java.io.IOException;

/**
 *
 * @author boi09
 */
public class Room {
    private int ID;
    private Client user1;
    private Client user2;
    private String password;
    private SQLHandler sqlhandler;

    public int getID() {
        return ID;
    }

    public Client getUser1() {
        return user1;
    }

    public Client getUser2() {
        return user2;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
    public Room(Client user1) {
        System.out.println("Tạo phòng thành công, ID là: "+RunServer.ID_room);
        this.password=" ";
        this.ID = RunServer.ID_room++;
        sqlhandler = new SQLHandler();
        this.user1 = user1;
        this.user2 = null;
    }
    
    public int getNumberOfUser(){
        return user2==null?1:2;
    }
    
    public void setUser2(Client user2){
        this.user2 = user2;
    }
    
    public void boardCast(String message){
        try {
            user1.write(message);
            user2.write(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public int getCompetitorID(int ID_ClientNumber){
        if(user1.getClientNumber()==ID_ClientNumber)
            return user2.getUser().getID();
        return user1.getUser().getID();
    }
    public Client getCompetitor(int ID_ClientNumber){
        if(user1.getClientNumber()==ID_ClientNumber)
            return user2;
        return user1;
    }
    
    public void setUsersToPlaying(){
        sqlhandler.updateToPlaying(user1.getUser().getID());
        if(user2!=null){
            sqlhandler.updateToPlaying(user2.getUser().getID());
        }
    }
    public void setUsersToNotPlaying(){
        sqlhandler.updateToNotPlaying(user1.getUser().getID());
        if(user2!=null){
            sqlhandler.updateToNotPlaying(user2.getUser().getID());
        }
    }

    
    public void increaseNumberOfGame(){
        sqlhandler.addGame(user1.getUser().getID());
        sqlhandler.addGame(user2.getUser().getID());
    }
    
    public void increaseNumberOfDraw(){
        sqlhandler.addDrawGame(user1.getUser().getID());
        sqlhandler.addDrawGame(user2.getUser().getID());
    }
    
    public void decreaseNumberOfGame(){
        sqlhandler.decreaseGame(user1.getUser().getID());
        sqlhandler.decreaseGame(user2.getUser().getID());
    }
}
