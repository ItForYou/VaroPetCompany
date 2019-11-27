package util.retrofit;

import java.util.ArrayList;

public class ServerPost {
    public String getSuccess() {
        return success;
    }
    public void setSuccess(String success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public ArrayList<MenuList> getMenu() {
        return menu;
    }
    public void setMenu(ArrayList<MenuList> menu) {
        this.menu = menu;
    }
    public ArrayList<TaxiPosition> getPosition() {return position;}

    public void setPosition(ArrayList<TaxiPosition> position) {this.position = position;}
    ArrayList<MenuList> menu;
    ArrayList<TaxiPosition> position;

    String success;
    String message;
}
