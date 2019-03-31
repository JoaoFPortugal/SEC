package hds_user;

public class Good {
	
	private int id;
    private String name;
    public boolean for_sale;
    
    public Good(int id, String name, boolean for_sale) {
    	this.id = id;
        this.name = name;
        this.for_sale = for_sale;
    }
}
