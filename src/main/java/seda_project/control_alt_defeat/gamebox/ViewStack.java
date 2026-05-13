package seda_project.control_alt_defeat.gamebox;

import java.util.Stack;

public class ViewStack {

    // Single shared instance
    private static ViewStack instance;

    // Private constructor prevents external instantiation
    private ViewStack() {
        this.fxmlLoaders = new Stack<>();
    }

    // Global access point
    public static ViewStack getInstance() {
        if (instance == null) {
            instance = new ViewStack();
        }
        return instance;
    }

    private Stack<String> fxmlLoaders;

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
