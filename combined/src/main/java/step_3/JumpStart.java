package step_3;

import org.opendolphin.binding.JFXBinder;
import org.opendolphin.core.PresentationModel;
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientPresentationModel;
import org.opendolphin.core.client.comm.JavaFXUiThreadHandler;
import org.opendolphin.core.client.comm.OnFinishedHandlerAdapter;
import org.opendolphin.core.comm.Command;
import org.opendolphin.core.comm.DefaultInMemoryConfig;
import org.opendolphin.core.comm.NamedCommand;
import org.opendolphin.core.server.comm.NamedCommandHandler;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class JumpStart extends Application {

    private static final String MODEL_ID = "modelId";
    private static final String MODEL_ATTRIBUTE_ID = "attrId";
    private static final String COMMAND_ID = "LogOnServer";

    private final DefaultInMemoryConfig config;
    private TextField textField;
    private Button button;
    private CheckBox status;
    private final PresentationModel textAttributeModel;

    public JumpStart() {
        config = new DefaultInMemoryConfig();
        textAttributeModel = config.getClientDolphin().presentationModel(MODEL_ID, new ClientAttribute(MODEL_ATTRIBUTE_ID, ""));
        config.getClientDolphin().getClientConnector().setUiThreadHandler(new JavaFXUiThreadHandler());
        config.getServerDolphin().registerDefaultActions();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = PaneBuilder.create().children(
                VBoxBuilder.create().children(
                        textField = TextFieldBuilder.create().build(),
                        button = ButtonBuilder.create().text("press me").build(),
                        HBoxBuilder.create().children(
                                LabelBuilder.create().text("IsDirty ?").build(),
                                status = CheckBoxBuilder.create().disable(true).build()
                        ).build()

                ).build()
        ).build();

        addServerSideAction();
        addClientSideAction();
        setupBinding();

        stage.setScene(new Scene(root, 300, 100));
        stage.show();
    }

    private void setupBinding() {
        JFXBinder.bind("text").of(textField).to(MODEL_ATTRIBUTE_ID).of(textAttributeModel);
        JFXBinder.bindInfo("dirty").of(textAttributeModel.getAt(MODEL_ATTRIBUTE_ID)).to("selected").of(status);
    }

    private void addClientSideAction() {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                config.getClientDolphin().send(COMMAND_ID, new OnFinishedHandlerAdapter() {
                    @Override
                    public void onFinished(List<ClientPresentationModel> presentationModels) {
                        textAttributeModel.getAt(MODEL_ATTRIBUTE_ID).rebase();
                    }
                });
            }
        });
    }

    private void addServerSideAction() {
        config.getServerDolphin().action(COMMAND_ID, new NamedCommandHandler() {
            @Override
            public void handleCommand(NamedCommand command, List<Command> response) {
                System.out.println(config.getServerDolphin().getAt(MODEL_ID).getAt(MODEL_ATTRIBUTE_ID).getValue());
            }
        });
    }

    public static void main(String[] args) {
        launch(JumpStart.class);
    }
}
