package hds_user;

public class Good {
	
    private String name;
    private String status;
    private int price;
    
    public Good(String name, int price){
        this.name = name;
        this.price = price;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getState(){
        return "Name: " + name + "\n" + "price: " + price + "\n" + "status: " + status;
    }
}
