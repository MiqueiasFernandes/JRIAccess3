/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live.writers;

/**
 *
 * @author mfernandes
 */
public abstract class AbstractWriter implements IWriter {

    protected String text;

    public AbstractWriter(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

}
