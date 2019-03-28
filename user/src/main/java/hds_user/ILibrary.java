package hds_user;

public interface ILibrary {
    String getStateofGood(Object o);
    boolean intentionToSell();
    boolean buyGood();
    boolean transferGood();
}
