package aircom.model;
/**
 * Im implementing a State design. Because doing a simultaneous interface would be challenging and not necessary at this poitn
 * Im gunna break turns into a"States", So while Player 1 is in attack mode, Player 2 is in defense. I might add another one
 * for transition or adding or reporting all the results from the observer. So I think I can do this with 3 states.
 * Attack and defense states basically helps control the game in a limited way and also synch the various inputs from the
 * two players... Also AI player?? but not sure how liekly it is to get that far. I also dont know how to connect
 * ,ultiplayer... i guess Ill learn
 */



public interface State {
    aircom.model.State execute();

}
