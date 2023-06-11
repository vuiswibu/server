/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author boi09
 */
public class ClientManager {
    private List<Client> listclient;

    public List<Client> getListServerThreads() {
        return listclient;
    }

    public ClientManager() {
        listclient = new ArrayList<>();
    }

    public void add(Client client){
        listclient.add(client);
    }
    
    public void mutilCastSend(String message){ 
        for(Client client : listclient){
            try {
                client.write(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void boardCast(int id, String message){
        for(Client client : listclient){
            if (client.getClientNumber() == id) {
                continue;
            } else {
                try {
                    client.write(message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public int getLength(){
        return listclient.size();
    }
    
    public void sendMessageToUserID(int id, String message){
        for(Client client : listclient){
            if(client.getUser().getID()==id){
                try {
                    client.write(message);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public Client getServerThreadByUserID(int ID){
        for(int i=0; i<listclient.size(); i++){
            if(listclient.get(i).getUser().getID()==ID){
                return listclient.get(i);
            }
        }
        return null;
    }
    
    public void remove(int id){
        for(int i=0; i<listclient.size(); i++){
            if(listclient.get(i).getClientNumber()==id){
                listclient.remove(i);
            }
        }
    }
}
