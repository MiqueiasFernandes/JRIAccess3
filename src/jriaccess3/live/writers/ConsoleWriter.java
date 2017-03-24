/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live.writers;

public class ConsoleWriter extends AbstractWriter {

    private final int type;

    public ConsoleWriter(int type, String text) {
        super(text);
        this.type = type;
    }

    @Override
    public String getTipo() {
        return "CONSOLE";
    }

    @Override
    public String getText() {
        return "[" + type + "]" + text;
    }

}
