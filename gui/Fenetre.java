package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import algo.GraphServer;
import algo.Request;
import algo.Response;
import algo.Server;

public class Fenetre extends JFrame{
    private JButton createServer,importGraph,exportPdf,purgeView,saveGraph;
    private Graph graph;
    private Viewer viewer;
    private ViewerPipe pipeIn;
    private Component view;
    private HashMap<String, ArrayList<String>> dns;
    private GraphServer graphServer;
    private JPopupMenu popUpMenu;
    private JMenuItem startOrShutdown, removeServer, addEdge, navigate, findPathIP;
    private Server selected;
    private ServerSocket socketServer;
    private HashMap<String, Socket> clientSockets;

    public Fenetre() throws IOException{
        init();

        JToolBar toolBar = new JToolBar();
        
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(view, BorderLayout.CENTER);

        toolBar.add(createServer);
        toolBar.add(importGraph);
        toolBar.add(saveGraph);
        toolBar.add(exportPdf);
        toolBar.add(purgeView);

        popUpMenu.add(startOrShutdown); 
        popUpMenu.add(removeServer);
        popUpMenu.add(addEdge);
        popUpMenu.add(navigate);
        popUpMenu.add(findPathIP); 
        
        setSize(800,800);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Simulateur Réseau");

        initEvents();

        while( true ) {
            pipeIn.pump();
            sleep( 40 );
        }
    }


    public void init(){
        createServer = new JButton(new ImageIcon("assets/server.png"));
        importGraph = new JButton(new ImageIcon("assets/import.png"));
        exportPdf = new JButton(new ImageIcon("assets/export-pdf.png"));
        purgeView = new JButton(new ImageIcon("assets/reload.png"));
        saveGraph = new JButton(new ImageIcon("assets/save.png"));
        
        graph = new SingleGraph("Network Simulator");
        viewer = new SwingViewer( graph, SwingViewer.ThreadingModel.GRAPH_IN_GUI_THREAD );
        pipeIn = viewer.newViewerPipe();
        view  = (Component) viewer.addDefaultView(false);
        popUpMenu = new JPopupMenu();

        selected = null;
        
        startOrShutdown = new JMenuItem("start/shutdown server");
        removeServer = new JMenuItem("Remove server");
        addEdge = new JMenuItem("Add Edge");
        navigate = new JMenuItem("Navigate");
        findPathIP = new JMenuItem("Find Shortest Path");

        createServer.setToolTipText("Add new server");
        exportPdf.setToolTipText("Export to pdf");
        importGraph.setToolTipText("Import from file");
        purgeView.setToolTipText("Reset graph");
        saveGraph.setToolTipText("Save the current graph");

        dns = new HashMap<>();
        graphServer = new GraphServer(new ArrayList<>());

        try{
            socketServer = new ServerSocket(GraphServer.SOCKET_SERVER_PORT);

        } catch(IOException e) {
            e.printStackTrace();
        }

        System.setProperty("org.graphstream.ui", "swing");
        graph.setAttribute("ui.stylesheet", GraphServer.styleSheet); 
        viewer.enableAutoLayout();

        pipeIn.addAttributeSink( graph );
        pipeIn.addViewerListener( new GraphViewerListener(this));
        pipeIn.pump();

        clientSockets = new HashMap<>();
        handleConnections();
    }

    /*
     * Ajoute les evenements de chaque boutons, menu dans la fenêtre
     */
    public void initEvents(){
        createServer.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> new AddServerModal(this,"Add Server"));
        });

        addEdge.addActionListener(evt -> {
            System.out.println(selected);
            if(graphServer.size() < 2)
                JOptionPane.showMessageDialog(this, "Il faut au minimum 2 serveurs pour définir un lien", "Add Edge error", JOptionPane.ERROR_MESSAGE);
            else   
                SwingUtilities.invokeLater(() -> new AddEdgeModal(this, "Add Edge"));
        });

        importGraph.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileFilter(new FileNameExtensionFilter(".gstream custom files", "gstream"));
            int returnVal = chooser.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                try{
                   System.out.printf("You choose %s\n",chooser.getSelectedFile().getAbsolutePath());
                    
                   purgeGraph();
                   
                   graphServer = new GraphServer(chooser.getSelectedFile().getAbsolutePath());

                    for(Server node : graphServer.getNodes()){
                        Node newNode = graph.addNode(node.getIpAddress());
                        newNode.setAttribute("ui.label", newNode.getId()); 
                        newNode.setAttribute("ui.class", "up");  
                        
                        if(!dns.containsKey(node.getDomainName()))
                            dns.put(node.getDomainName(), new ArrayList<>());
                        
                        dns.get(node.getDomainName())
                           .add(node.getIpAddress());
                    }

                    for(Server node : graphServer.getNodes()){
                        for(Server neighbor : node.getAllReachableServer()){
                            if(!graph.edges().anyMatch(edge -> edge.getId().equals(String.format("%s->%s",neighbor.getIpAddress(),node.getIpAddress()))))
                                graph.addEdge(String.format("%s->%s",node.getIpAddress(),neighbor.getIpAddress()),node.getIpAddress(), neighbor.getIpAddress())
                                     .setAttribute("ui.label", neighbor.getDistFromReachableServer(node));
                        }
                    } 
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        });

        navigate.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> new NavigateModal(this, String.format("%s's navigator",selected.getIpAddress()), String.format("<html>Welcome to <strong>%s</strong> navigator</html>", selected.getIpAddress())));
        });

        findPathIP.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> new FindPathModal(this, "Find shortest path to an IP adress"));
        });

        exportPdf.addActionListener(evt -> {
            BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
            view.paint(image.createGraphics());
            JFileChooser saveModal = new JFileChooser(new File("screenshots"));
 
            try {           
                saveModal.setDialogTitle("Export to pdf");
                saveModal.setFileFilter(new FileNameExtensionFilter("pdf files", "pdf"));
                
                int result = saveModal.showSaveDialog(this);

                if(result == JFileChooser.APPROVE_OPTION){
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bytes);
                    bytes.flush();
                    byte[] imageBytes = bytes.toByteArray();
                    bytes.close();
                    
                    PdfDocument pdfDoc = new PdfDocument(new PdfWriter (new FileOutputStream(saveModal.getSelectedFile())));
                    Document document = new Document(pdfDoc);
                    Image img = new Image(ImageDataFactory.create(imageBytes));
                    
                    document.add(img);
                    document.close();
                    System.out.printf("Screenshot saved as %s\n",saveModal.getSelectedFile().getName());
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                if(SwingUtilities.isRightMouseButton(e) && selected != null)
                    popUpMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        startOrShutdown.addActionListener(evt -> {
            selected.setIsUp(!selected.getIsUp());
            graph.getNode(selected.getIpAddress())
                 .setAttribute("ui.class", selected.getIsUp() ? "up" : "down" );

            selected = null;
        });

        removeServer.addActionListener(evt ->{
            int confirm = JOptionPane.showConfirmDialog(this, String.format("Remove server %s?",selected.getIpAddress()), "Remove Server", JOptionPane.YES_NO_OPTION);

            if(confirm == 0){
                deleteServer(selected.getIpAddress());
                JOptionPane.showMessageDialog(this, "Server removed successfully!", "Remove server", JOptionPane.INFORMATION_MESSAGE);
            }
            else
                selected = null;
            
        });

        purgeView.addActionListener(evt -> {
            int confirm = JOptionPane.showConfirmDialog(this, String.format("Reset the graph? You'll lose all unsaved work"), "Reset graph", JOptionPane.YES_NO_OPTION);

            if(confirm == 0){
                purgeGraph();
                JOptionPane.showMessageDialog(this, "Server removed successfully!", "Remove server", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        saveGraph.addActionListener(evt -> {
            JFileChooser saveModal = new JFileChooser(new File("."));
 
            try {           
                saveModal.setDialogTitle("Save to gstream custom file");
                saveModal.setFileFilter(new FileNameExtensionFilter("gstream custom files", "gstream"));
                
                int result = saveModal.showSaveDialog(this);

                if(result == JFileChooser.APPROVE_OPTION)
                    save(saveModal.getSelectedFile().getAbsolutePath());
            }catch(IOException e){
                e.printStackTrace();
            }
        });
    }

    public void purgeGraph(){
        if(graphServer != null){
            for(Server server : graphServer.getNodes()){
                for(Server neighbor : server.getAllReachableServer()){
                    graph.removeEdge(server.getIpAddress(), neighbor.getIpAddress());
                    
                    neighbor.getAllReachableServer()
                        .remove(server);
                }
    
                graph.removeNode(server.getIpAddress());
                server.getAllReachableServer()
                      .removeIf(e -> true);
            }
    
            System.out.println("Nodes:");
    
            graph.nodes()   
                 .forEach(System.out::println);
    
            System.out.println("Edges:");
    
            graph.edges()
                 .forEach(System.out::println);
            
            graphServer.getNodes()
                       .removeAll(graphServer.getNodes());
            
            System.out.println(graphServer.getNodes());

            dns = new HashMap<>();
        }
    }


    public void addNode(String ipAdress, String domainName){
        if(!graphServer.containsServerByIpAdress(ipAdress)){
            Server newServer = new Server(ipAdress,domainName);
            Node server = graph.addNode(ipAdress);
            server.setAttribute("ui.label", server.getId());
            server.setAttribute("ui.class", "up");

            graphServer.addNode(newServer);

            if(!dns.containsKey(domainName))
                dns.put(domainName, new ArrayList<>());
            
            dns.get(domainName)
               .add(ipAdress);
            
            System.out.println(graphServer.getNodes());
            System.out.println(dns);
        }        
    }

    /*
     * Rajouter un chemin entre 2 serveurs ayant pour ip ipSource et ipDest de poids weight
     */
    public void addEdge(String ipSource, String ipDest, int weight){
        Server sourceServer = graphServer.findServerByIpAdress(ipSource)
                                         .orElse(null);
        Server destServer = graphServer.findServerByIpAdress(ipDest)
                                       .orElse(null);


        destServer.addServerReachable(sourceServer,weight);
        sourceServer.addServerReachable(destServer, weight);

        graph.addEdge(String.format("%s->%s",ipSource,ipDest), graph.getNode(ipSource), graph.getNode(ipDest))
             .setAttribute("ui.label", weight); 
    }

    public void deleteServer(String ip){
        Server server = graphServer.findServerByIpAdress(ip)
                                   .orElse(null);

        for(Server neighbor : server.getAllReachableServer()){
            neighbor.removeServerReachable(server);
            graph.removeEdge(ip, neighbor.getIpAddress());
        }

        graph.removeNode(ip);
        server.getAllReachableServer()
              .removeIf(e -> true);

        System.out.println("Nodes:");

        graph.nodes()   
            .forEach(System.out::println);

        System.out.println("Edges:");

        graph.edges()
            .forEach(System.out::println);
        
        graphServer.getNodes()
            .remove(server);
        
        dns.get(server.getDomainName())
           .remove(server.getIpAddress());

        System.out.println(graphServer.getNodes());
    }

    public void selectServer(String ip){
        selected = graphServer.findServerByIpAdress(ip)
                                   .orElse(null);
    }

    public void displayPopUp() {   
        startOrShutdown.setText(selected.getIsUp() ? "Shutdown" : "Start");
    }

    public void deselect() { 
        selected = null;
    }

    public Server getSelectedServer(){ 
        System.out.println(selected == null ? "null" : selected.getIpAddress() );
        return selected; 
    }

    public String[] getServersIp(){
        String[] serversIp = new String[graphServer.size()];

        for(int i=0; i<graphServer.size(); i++)
            serversIp[i] = graphServer.get(i)
                                  .getIpAddress();
        
        return serversIp;
    }

    public String[] getAllDomainName() { 
        System.out.println(dns);
        return  dns.keySet()
                    .toArray(new String[dns.size()]); 
    }

    public ArrayList<String> getIpOfDomainName(String domainName) { return dns.get(domainName); }


    public static void sleep( int ms ){
        try{
            Thread.sleep(ms);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /*
     * Affiche le chemin le plus court si il existe du serveur ipSource vers ipDest
     */
    public void showPathToIP(String ipSource, String ipDest){
        ArrayList<String> path = graphServer.getShortestPath(ipSource, ipDest);

        graph.edges()
             .forEach(edge -> edge.setAttribute("ui.class", ""));     

        if(path.size() > 0){
            for(int i=0; i<path.size()-1 ; i++){
                if(graph.getEdge(String.format("%s->%s",path.get(i),path.get(i+1))) != null)
                    graph.getEdge(String.format("%s->%s",path.get(i),path.get(i+1)))
                         .setAttribute("ui.class", "marked");
                else
                    graph.getEdge(String.format("%s->%s",path.get(i+1),path.get(i)))
                         .setAttribute("ui.class", "marked");
            }
        }

        else
            JOptionPane.showMessageDialog(null, String.format("The server %s is not reachable from %s",ipDest,ipSource),"Not reachable!",JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * Recherche du chemin vers un url (nom de domaine ou ip)
     */
    public ArrayList<String> navigatorSearch(String ipSource, String url){
        if(dns.containsKey(url)){
            ArrayList<String> path = graphServer.getShortestPath(ipSource, dns.get(url));  

            return path.size() != 0 ? path : null;
        }

        else{
            if(Server.isIpAdress(url)){
                ArrayList<String> path = graphServer.getShortestPath(ipSource, url);
                
                return path.size() != 0 ? path : null;
            }
        }
        
        return null;
    }

    /*
     * Sauvegarder l'état du graphe actuel 
     */
    public void save(String path) throws IOException{
        PrintWriter out = new PrintWriter(new File(path));
        
        out.println(graphServer.size());

        for(Server server : graphServer.getNodes())
            out.printf("%s %s\n",server.getDomainName(),server.getIpAddress());
        
        graph.edges()   
             .forEach(edge -> out.printf("%s -> %s %s\n",edge.getNode0(),edge.getNode1(), edge.getAttribute("ui.label")));
        
        JOptionPane.showMessageDialog(this, "Current graph saved successfully", "Saved successfully", JOptionPane.INFORMATION_MESSAGE);
        out.close();
    }

    public GraphServer getGraphServer() { return graphServer; }

    public Socket getClientSocket(String ip) { return clientSockets.get(ip); }

    public HashMap<String, Socket> getClientSockets() { return clientSockets; }

    //Connecter les sockets de chaque instance de Server au socketServer
    public void handleConnections(){
        new Thread(()->{
            try{
                while(true){
                    Socket clientSocket = socketServer.accept();
                   
                    handleRequest(clientSocket);
                }
            }catch(IOException e){}
        }).start();
    }

    public void handleRequest(Socket socket){
        new Thread(() -> {
            try{
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String ip = input.readLine();
                System.out.printf("New server %s\n",ip);
                
                clientSockets.put(ip, socket);
                
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                while(true){
                    Request req = (Request) in.readObject();

                    System.out.printf("[%s] Request received: %s\n", req.getIpDest(), req);

                    try{
                        ObjectOutputStream out = new ObjectOutputStream(clientSockets.get(req.getIpSource()).getOutputStream());
                        
                        if(req.getHeader().size() == 1){
                            String destHtml = graphServer.findServerByIpAdress(req.getIpDest())
                                                     .orElse(null)
                                                     .getHtmlPageContent();
                            
                            out.writeObject(new Response(req.getIpDest(), req.getIpSource(), req.getHeader(), destHtml));
                            out.flush();
                        }
                        else{
                            String currentIp = req.getHeader()
                               .removeFirst();
                            
                            Server nextServer = graphServer.findServerByIpAdress(req.getHeader().getFirst())
                                                           .orElse(null);
                                        
                            Server currentServer = graphServer.findServerByIpAdress(currentIp)
                                                              .orElse(null);
                            
                            currentServer.sendRequest(nextServer, req.getHeader());

                            ObjectInputStream clientInput = new ObjectInputStream(currentServer.getInputStream());

                            Response res = (Response) clientInput.readObject();
                            
                            System.out.printf("[%s] Response Received: %s\n",currentIp,res);
                            res.setIpSource(currentIp);

                            res.setIpDest(req.getIpSource());

                            res.getHeader()
                               .addFirst(currentIp);

                            out.writeObject(res);
                            out.flush();
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }).start();
    }
}
