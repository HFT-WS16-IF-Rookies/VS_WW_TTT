
package de.wiest_lukas.wolf_erik;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

public class TicTacToeGUI
{
    protected static JTextPane status;
    protected static Thread server;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocation(200, 200);
        frame.setVisible(true);

        JPanel container = new JPanel(new BorderLayout());
        frame.add(container, BorderLayout.CENTER);

        container.setPreferredSize(new Dimension (200, 200));
        container.setBackground(Color.BLACK);

        // Main Menu
        JMenu mainMenu = new JMenu("Datei");
        JMenuItem mainMenu_Exit = new JMenuItem("Beenden");
        mainMenu_Exit.addActionListener(al ->
        {
            System.exit(0);
        });

        JMenuItem mainMenu_newGame = new JMenuItem("Neues Spiel...");
        mainMenu_newGame.addActionListener(al ->
        {
            String serverURL = JOptionPane.showInputDialog("Server-URL:");
            try
            {
                Thread game = new Thread(new TTTGame(serverURL));
                game.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        mainMenu.add(mainMenu_newGame);

        mainMenu.add(mainMenu_Exit);

        // Server Menu
        JMenu serverMenu = new JMenu("Server");
        JMenuItem serverMenu_provideServer = new JMenuItem("Server bereitstellen...");
        JMenuItem serverMenu_stopServer = new JMenuItem("Server stoppen...");
        serverMenu_provideServer.addActionListener(al ->
        {
            server = new Thread(new TTTServer(status));
            server.start();
            serverMenu_provideServer.setEnabled(false);
            serverMenu_stopServer.setEnabled(true);
        });
        serverMenu.add(serverMenu_provideServer);

        serverMenu_stopServer.setEnabled(false);
        serverMenu_stopServer.addActionListener(al ->
        {
            server.interrupt(); // search for good solution in server code later
            serverMenu_provideServer.setEnabled(true);
            serverMenu_stopServer.setEnabled(false);
        });
        serverMenu.add(serverMenu_stopServer);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(mainMenu);
        menuBar.add(serverMenu);

        container.add(menuBar, BorderLayout.NORTH);

        status = new JTextPane();
        status.setEditable(false);
        container.add(status, BorderLayout.SOUTH);

        JPanel field = new JPanel(new GridLayout(3, 3));
        container.add(field, BorderLayout.CENTER);
        JButton[] cells = new JButton[9];
        for (int i=0; i < cells.length; i++)
        {
            cells[i] = new JButton(String.valueOf(i+1));
            field.add(cells[i]);
        }

        frame.pack();
    }
}

class TTTGame implements Runnable
{
    String serverURL;
    Socket server;
    ObjectInputStream in;
    ObjectOutputStream out;

    public TTTGame(String serverURL) throws UnknownHostException, IOException
    {
        this.serverURL = serverURL;
    }

    @Override
    public void run()
    {
        try
        {
            String[] address = serverURL.split(":");
            server = new Socket(address[0], Integer.valueOf(address[1]));
            out = new ObjectOutputStream(server.getOutputStream());
            out.flush();
            in = new ObjectInputStream(server.getInputStream());

            TTTProtocol telegram = new TTTProtocol();
            telegram.message = "hi from Client";
            out.writeObject(telegram);
            out.flush();
            out.writeObject(null);
            out.flush();

            try
            {
                Object obj;
                while ((obj=in.readObject()) != null && obj instanceof TTTProtocol)
                    System.out.println("[Client] - " + ((TTTProtocol) obj).message);
            }
            catch (IOException|ClassNotFoundException e)
            {
                e.printStackTrace();
            }

            in.close();
            out.close();
            server.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}