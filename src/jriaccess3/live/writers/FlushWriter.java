/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live.writers;

public class FlushWriter implements IWriter {

    @Override
    public String getTipo() {
        return "FLUSH";
    }

    @Override
    public String getText() {
        return "";
    }

}
