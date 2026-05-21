package seda_project.control_alt_defeat.gamebox.ui;
// Source - https://stackoverflow.com/a/12851162
// Found over - https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;


public class IntField extends TextField {
    private int value;


    // expose an integer value property for the text field.
    public int  getValue()                 { return this.value; }
    public void setValue(int newValue)     { this.value = newValue; }

    public IntField() {
        // restrict key input to numerals.
        this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!"0123456789".contains(keyEvent.getCharacter())) {
                    keyEvent.consume();
                }
            }
        });
    }
}
