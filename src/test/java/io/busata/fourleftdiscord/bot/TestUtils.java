package io.busata.fourleftdiscord.bot;


import org.junit.jupiter.api.Test;

public class TestUtils {


    @Test
    public void testSplit() {
        String[] split = "sakukoivu;mjh43;JamesF890;Dimzon68;Chalmers78;Ludo55200;Cycl0nic;WhiteFemale;James__McAdam;Borisssito;schmidty_22;adida774;schiffig6r;FSARacing;Dunoon1956;durandom;Jits00;stockenstein;SaltyEnferno;m6nop;Akmotorsport;RIOT_Nova935;MrTGUK;BoringDamo;shinyo;VoidRob0t;Weepy".split(";");

        for (int i = 0; i < split.length; i++) {
            System.out.println(split[i]);
        }
    }
}
