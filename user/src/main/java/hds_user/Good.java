package hds_user;

public class Good {
	
	private int id;
	private int owner;
    private String name;
    public boolean for_sale;
    
    public Good(int id, String name, int owner, boolean for_sale) {
    	this.id = id;
    	this.owner = owner;
        this.name = name;
        this.for_sale = for_sale;
    }
}
