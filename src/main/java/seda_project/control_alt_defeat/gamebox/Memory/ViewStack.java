package seda_project.control_alt_defeat.gamebox.Memory;

import javafx.fxml.FXMLLoader;

import java.util.Stack;

public class ViewStack {
    private Stack<String> fxmlLoaders;

    public ViewStack() {
        this.fxmlLoaders = new Stack<>();
    }

    public String getFxmlLoader() {
        return fxmlLoaders.peek();
    }

    public void addFxmlLoaders(String fxmlLoaders) {
        this.fxmlLoaders.add(fxmlLoaders);
    }

    public void popFxmlLoader(){
        this.fxmlLoaders.pop();
    }

    public void emtyStack() {
        this.fxmlLoaders = new Stack<>();
    }
}
