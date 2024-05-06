package gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import algo.GraphServer;
import algo.Server;

public class Fenetre extends JFrame{
    private JButton createServer, addEdge, importGraph, findShortestPath, exportPdf;
    private Graph graph;
    private Viewer viewer;
    private ViewerPipe pipeIn;
    private Component view;
    private HashMap<String, ArrayList<String>> dns;
    private GraphServer graphServer;
    ServerInfo serverInfo;
    
    public Fenetre() throws IOException{
        init();

        JToolBar toolBar = new JToolBar();
    
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(view, BorderLayout.CENTER);
        //getContentPane().add(serverInfo, BorderLayout.WEST);

        toolBar.add(createServer);
        toolBar.add(addEdge);
        toolBar.add(importGraph);
        toolBar.add(findShortestPath);
        toolBar.add(exportPdf);
        
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
        createServer = new JButton("Add Server");
        addEdge = new JButton("Add edge");
        importGraph = new JButton("Import from file");
        findShortestPath = new JButton("Find Path");
        exportPdf = new JButton("Export to pdf");
        graph = new SingleGraph("Network Simulator");
        viewer = new SwingViewer( graph, SwingViewer.ThreadingModel.GRAPH_IN_GUI_THREAD );
        pipeIn = viewer.newViewerPipe();
        view  = (Component) viewer.addDefaultView(false);
        serverInfo = new ServerInfo();
        

        dns = new HashMap<>();
        graphServer = new GraphServer(new ArrayList<>());

        System.setProperty("org.graphstream.ui", "swing");
        graph.setAttribute("ui.stylesheet", GraphServer.styleSheet); 
        viewer.enableAutoLayout();

        pipeIn.addAttributeSink( graph );
        pipeIn.addViewerListener( new GraphViewerListener(this));
        pipeIn.pump();
    }


    public void initEvents(){
        createServer.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> new AddServerModal(this,"Add Server"));
        });

        addEdge.addActionListener(evt -> {
            if(graphServer.size() < 2)
                JOptionPane.showMessageDialog(this, "Il faut au minimum 2 serveurs pour définir un lien", "Add Edge error", JOptionPane.ERROR_MESSAGE);
                            else   
                SwingUtilities.invokeLater(() -> new AddEdgeModal(this, "Add Edge"));
        });

        importGraph.addActionListener(evt -> {
            JFileChooser chooser = new JFileChooser(".");

            int returnVal = chooser.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                try{
                   System.out.printf("You choose %s\n",chooser.getSelectedFile().getAbsolutePath());
                    
                   purgeGraph();
                   
                   graphServer = new GraphServer(chooser.getSelectedFile().getAbsolutePath());

                    for(Server node : graphServer.getNodes()){
                        Node newNode = graph.addNode(node.getIpAddress());
                        newNode.setAttribute("ui.label", newNode.getId());     
                    }

                    for(Server node : graphServer.getNodes()){
                        for(Server neighbor : node.getAllReachableServer()){
                            if(!graph.edges().anyMatch(edge -> edge.getId().equals(String.format("%s->%s",neighbor.getIpAddress(),node.getIpAddress()))))
                                graph.addEdge(String.format("%s->%s",node.getIpAddress(),neighbor.getIpAddress()),node.getIpAddress(), neighbor.getIpAddress());
                        }
                    } 

                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        });

        findShortestPath.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> new FindPathModal(this, "Find shortest path"));
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
    }

    public void purgeGraph(){
        for(Server server : graphServer.getNodes()){
            for(Server neighbor : server.getAllReachableServer()){
                graph.removeEdge(server.getIpAddress(), neighbor.getIpAddress());
                graph.removeEdge(neighbor.getIpAddress(), server.getIpAddress());
                neighbor.getAllReachableServer()
                        .remove(server);
            }

            graph.removeNode(server.getIpAddress());
            server.getAllReachableServer()
                  .removeIf(e -> true);
        }

        graphServer.getNodes()
             .removeIf(e -> true);
    }


    public void addNode(String ipAdress, String domainName){
        if(!graphServer.containsServerByIpAdress(ipAdress)){
            Server newServer = new Server(ipAdress,domainName);
            Node server = graph.addNode(ipAdress);
            server.setAttribute("ui.label", server.getId());

            graphServer.addNode(newServer);

            if(!dns.containsKey(domainName))
                dns.put(domainName, new ArrayList<>());
            
            dns.get(domainName)
               .add(ipAdress);
            
            System.out.println(graphServer.getNodes());
            System.out.println(dns);
        }        
    }

    public void addEdge(String ipSource, String ipDest, int weight){
        Server sourceServer = graphServer.findServerByIpAdress(ipSource)
                                         .orElse(null);
        Server destServer = graphServer.findServerByIpAdress(ipDest)
                                       .orElse(null);


        destServer.addServerReachable(sourceServer,weight);
        sourceServer.addServerReachable(destServer, weight);

        graph.addEdge(String.format("%s->%s",sourceServer,destServer), graph.getNode(ipSource), graph.getNode(ipDest));      
        graph.addEdge(String.format("%s->%s",destServer,sourceServer), graph.getNode(ipDest), graph.getNode(ipSource));   
    }

    public void selectServer(String ip){
        Server server = graphServer.findServerByIpAdress(ip)
                                   .orElse(null);
        serverInfo.selected(server);
    }

    public String[] getServersIp(){
        String[] serversIp = new String[graphServer.size()];

        for(int i=0; i<graphServer.size(); i++)
            serversIp[i] = graphServer.get(i)
                                  .getIpAddress();
        
        return serversIp;
    }

    public Set<String> getAllDomainName(){
        return dns.keySet();
    }


    public static void sleep( int ms ){
        try{
            Thread.sleep(ms);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public void showPath(String ipSource, String ipDest){
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

                sleep(100);
            }
        }

        else
            JOptionPane.showMessageDialog(null, String.format("Il n'y a pas de chemin allant de %s à %s",ipSource,ipDest),"Pas de chemin!",JOptionPane.INFORMATION_MESSAGE);
    }
}