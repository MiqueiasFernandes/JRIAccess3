/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live.writers;

public class EchoWriter extends AbstractWriter {

    public EchoWriter(String text) {
        super(text);
    }

    @Override
    public String getTipo() {
        return "ECHO";
    }

}
