package hds_user;

public class Good {
	
	private int id;
	private int owner;
	private boolean for_sale;
    
    public Good(int id, int owner, boolean for_sale) {
    	this.id = id;
    	this.owner = owner;
        this.for_sale = for_sale;
    }
    
    public int getID() { return id; }
    public int getOwner() { return owner; }
    public boolean getForSale() { return for_sale; }
    
    public boolean setForSale() { return for_sale; }
}
