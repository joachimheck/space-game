package org.heckcorp.domination.desktop;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.heckcorp.domination.Constants;
import org.heckcorp.domination.DefaultModel;
import org.heckcorp.domination.GameModel;
import org.heckcorp.domination.ModelInitializer;
import org.heckcorp.domination.NewGameInitializer;
import org.heckcorp.domination.Unit;
import org.heckcorp.domination.ViewMonitor;
import org.heckcorp.domination.desktop.view.SequentialExecutor;
import org.heckcorp.domination.desktop.view.SwingView;
import org.heckcorp.domination.desktop.view.UIResources;
import org.heckcorp.domination.desktop.view.Util;

/**
 * Domination serves as the controller component of the application.
 * It manages communications and control between the UIManager and
 * GameManager classes, which serve respectively as the view and
 * model components.
 * 
 * @author Joachim Heck
 *
 */
@SuppressWarnings("serial")
public class Domination extends JPanel implements ViewMonitor {

    /**
     * This convenience class exists primarily to reduce clutter
     * in the parent class.  It handles action mappings.
     * 
     * @author Joachim Heck
     */
    private final class ActionManager {
        public void manage() {
            defineActions();
            defineKeyMaps();
        }

        private void defineActions() {
            getActionMap().put(Constants.ACTION_SKIP_UNIT, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    skipSelectedUnit();
                }
            });
            getActionMap().put(Constants.ACTION_SLEEP_UNIT, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    sleepSelectedUnit();
                }
            });
            getActionMap().put(Constants.ACTION_WAIT_UNIT, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    waitSelectedUnit();
                }
            });
            getActionMap().put(Constants.ACTION_SAVE_GAME, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    saveGame();
                }
            });
            getActionMap().put(Constants.ACTION_LOAD_GAME, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    loadGame();
                }
            });
            getActionMap().put(Constants.ACTION_END_TURN, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    executor.execute(new Runnable() {
                        public void run() {
                            model.endTurn();
                        }
                    });
                }
            });
            getActionMap().put(Constants.ACTION_TOGGLE_HEX_HIDING, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    executor.execute(new Runnable() {
                        public void run() {
                            model.toggleHexHiding();
                        }
                    });
                }
            });
        }

        private void defineKeyMaps() {
            getInputMap().put(KeyStroke.getKeyStroke("SPACE"), Constants.ACTION_SKIP_UNIT);
            getInputMap().put(KeyStroke.getKeyStroke("S"), Constants.ACTION_SLEEP_UNIT);
            getInputMap().put(KeyStroke.getKeyStroke("W"), Constants.ACTION_WAIT_UNIT);
            getInputMap().put(KeyStroke.getKeyStroke("ENTER"), Constants.ACTION_END_TURN);
            getInputMap().put(KeyStroke.getKeyStroke("T"), Constants.ACTION_TOGGLE_HEX_HIDING);
        }

    }

    protected void displayAboutPopup() {
        String message = Constants.GAME_NAME + " version " +
        Constants.MAJOR_VERSION + "." + Constants.MINOR_VERSION +
        "\nBy Joachim Heck";
        JOptionPane.showMessageDialog(this, message, "About " + Constants.GAME_NAME,
                                      JOptionPane.INFORMATION_MESSAGE);
    }
    
    protected void displayTextPopup(String filename) {
        JTextArea textArea = new JTextArea();
        JScrollPane scroller = new JScrollPane(textArea);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                Util.getResource(filename)));

            int maxColumns = 0;
            StringBuffer buffer = new StringBuffer();
            String line = in.readLine();
            while (line != null) {
                buffer.append(line).append('\n');
                maxColumns = Math.max(maxColumns, line.length());
                line = in.readLine();
            }

            textArea.setRows(40);
            textArea.setText(buffer.toString());
            textArea.setCaretPosition(0);
        } catch (FileNotFoundException e) {
            log.severe("Couldn't open resource: " + Constants.RELEASE_NOTES_FILE);
        } catch (IOException e) {
            log.severe("Couldn't read from resource: " + Constants.RELEASE_NOTES_FILE);
        }

        JOptionPane.showMessageDialog(this, scroller);
    }

    protected void skipSelectedUnit() {
        executor.execute(new Runnable() {
            public void run() {
                model.skipSelectedUnit();
            }
        });
    }
    
    protected void sleepSelectedUnit() {
        executor.execute(new Runnable() {
            public void run() {
                model.sleepSelectedUnit();
            }
        });
    }

    protected void waitSelectedUnit() {
        executor.execute(new Runnable() {
            public void run() {
                model.waitSelectedUnit();
            }
        });
    }

    private JMenu cityMenu;
    private SequentialExecutor executor;

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem item = new JMenuItem("Open");
        item.setMnemonic(KeyEvent.VK_O);
        item.addActionListener(getActionMap().get(Constants.ACTION_LOAD_GAME));
        fileMenu.add(item);

        item = new JMenuItem("Save");
        item.setMnemonic(KeyEvent.VK_S);
        item.addActionListener(getActionMap().get(Constants.ACTION_SAVE_GAME));
        fileMenu.add(item);

        item = new JMenuItem("Exit");
        item.setMnemonic(KeyEvent.VK_X);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(item);

        JMenu unitMenu = new JMenu("Unit");
        unitMenu.setMnemonic(KeyEvent.VK_U);

        item = new JMenuItem("Wait");
        item.setMnemonic(KeyEvent.VK_W);
        item.addActionListener(getActionMap().get(Constants.ACTION_WAIT_UNIT));
        unitMenu.add(item);

        item = new JMenuItem("Skip");
        item.setMnemonic(KeyEvent.VK_K);
        item.addActionListener(getActionMap().get(Constants.ACTION_SKIP_UNIT));
        unitMenu.add(item);

        item = new JMenuItem("Sleep");
        item.setMnemonic(KeyEvent.VK_S);
        item.addActionListener(getActionMap().get(Constants.ACTION_SLEEP_UNIT));
        unitMenu.add(item);
        
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        item = new JMenuItem("End Turn");
        item.setMnemonic(KeyEvent.VK_E);
        item.addActionListener(getActionMap().get(Constants.ACTION_END_TURN));
        gameMenu.add(item);

        cityMenu = new JMenu("City Production");
        gameMenu.add(cityMenu);

        // Add an item for each type of unit to the production menu.
        for (final Unit.Type type : Unit.Type.values()) {
            item = new JMenuItem(type.name);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    model.setSelectedCityProductionType(type);
                }
            });
            cityMenu.add(item);
        }

        item = new JMenuItem("Toggle Hex Hiding");
        item.setMnemonic(KeyEvent.VK_H);
        item.addActionListener(getActionMap().get(Constants.ACTION_TOGGLE_HEX_HIDING));
        gameMenu.add(item);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        item = new JMenuItem("About");
        item.setMnemonic(KeyEvent.VK_A);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayAboutPopup();
            }
        });
        helpMenu.add(item);
        
        item = new JMenuItem("Release Notes");
        item.setMnemonic(KeyEvent.VK_R);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTextPopup(Constants.RELEASE_NOTES_FILE);
            }
        });
        helpMenu.add(item);

        item = new JMenuItem("Instructions");
        item.setMnemonic(KeyEvent.VK_I);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayTextPopup(Constants.INSTRUCTIONS_FILE);
            }
        });
        helpMenu.add(item);

        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        menuBar.add(unitMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private Domination() {
        try {
            UIResources.initializeResources();
        } catch (IOException e) {
            System.err.println("Could not initialize Domination!");
            e.printStackTrace();
            System.exit(-1);
        }
 
        BorderLayout layout = new BorderLayout();
        setLayout(layout);

        createViewAndModel(null);
        
        new ActionManager().manage();
    }

    /**
     * Creates a new SwingView and DefaultModel and assigns them to the appropriate
     * fields.
     * @param in if not null, an ObjectInputStream from which the new model will
     *   read its data.
     */
    private void createViewAndModel(final ObjectInputStream in) {
        if (view != null) {
            remove(view);
        }

        view = new SwingView();
        view.setBorder(BorderFactory.createRaisedBevelBorder());
        add(view);
        view.setMonitor(this);
        
        // All calls to the model must go through the executor,
        // so it doesn't call into the view on the AWT thread.
        executor = SequentialExecutor.getInstance();
        
        executor.execute(new Runnable() {
            public void run() {
                if (in == null) {
                    log.fine("Creating new game model.");
                    model = new DefaultModel();
                } else {
                    try {
                        log.fine("Loading game model from file.");
                        model = new DefaultModel();
                        DefaultModel.GameStateManager gsm =
                            ((DefaultModel) model).getGameStateManager();
                        gsm.setInputStream(in);
                        gsm.initializeModel(model, view);
                        validate();
                        log.fine("Finished loading.");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void windowOpened() {
        executor.execute(new Runnable() {
            public void run() {
                ModelInitializer initializer =
                    new NewGameInitializer(Constants.MAP_WIDTH,
                                           Constants.MAP_HEIGHT);
                try {
                    initializer.initializeModel(model, view);
                } catch (Exception e) {
                    // This initializer doesn't actually throw the exceptions it claims.
                    assert false;
                }
            }
        });
        
        view.message("Game Started");
    }

    private static Domination instance = null;
    
    /**
     * 
     * @return
     * @pre createInstance() has already been called.
     */
    public static Domination getInstance() {
        if (instance == null) {
            instance = new Domination();
        }
        return instance;
    }

    public static void main(String[] args) {
        // Initialize logging.
        String filename = args.length > 0 ? args[0] :
            Constants.LOGGING_CONFIG_FILE;

        try {
            InputStream logConfigIn = Util.getResource(filename);
            LogManager.getLogManager().readConfiguration(logConfigIn);
        } catch (SecurityException e) {
            assert false;
        } catch (IOException e) {
            if (args.length > 0) {
                System.err.println("Couldn't read configuration file: " +
                                   e.getMessage());
                System.err.println("Usage: java -jar Domination.jar [configfile]");
            } else {
                e.printStackTrace();
            }
            System.exit(0);
        }
        

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame(Constants.GAME_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Domination.getInstance().windowOpened();
            }
        });

        frame.setJMenuBar(Domination.getInstance().createMenuBar());
        frame.setContentPane(Domination.getInstance());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    protected void loadGame() {
        File saveFile = new File(Constants.GAME_NAME + ".sav");
        log.info("Loading game from " + saveFile.getName());

        ObjectInputStream in = null;
        try {
            // Shut down the current game first.
            model.getTurnManager().interrupt();
            
            in = new ObjectInputStream(new FileInputStream(saveFile));
            createViewAndModel(in);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void saveGame() {
        File saveFile = new File(Constants.GAME_NAME + ".sav");
        log.info("Saving game in " + saveFile.getName());
        ObjectOutputStream out = null;
        
        try {
            out = new ObjectOutputStream(new FileOutputStream(saveFile));
            ((DefaultModel) model).getGameStateManager().write(out);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private GameModel model;
    private SwingView view;

    public void hexClicked(final Point hexPos, int button) {
        if (enabled) {
            if (button == MouseEvent.BUTTON1) {
                // Button 1 selects.
                executor.execute(new Runnable() {
                    public void run() {
                        model.selectHex(hexPos);

                        if (model.getSelectedCity() == null) {
                            // Disable unit production menu 
                            cityMenu.setEnabled(false);
                        } else {
                            // Enable it.
                            cityMenu.setEnabled(true);
                        }
                    }
                });
            } else if (button == MouseEvent.BUTTON2) {
                // Button 2 does nothing.
            } else if (button == MouseEvent.BUTTON3) {
                // Button 3 moves a unit.
                executor.execute(new Runnable() {
                    public void run() {
                        model.setSelectedUnitDestination(hexPos);
                        model.moveSelectedUnit();
                    }
                });
            }
        } else {
            log.fine("Mouse click ignored - player is not active.");
        }
    }

    private boolean enabled = false;
    private static Logger log = Logger.getLogger(Domination.class.getName());
}

