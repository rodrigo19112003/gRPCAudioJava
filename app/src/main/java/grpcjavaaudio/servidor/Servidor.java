package grpcjavaaudio.servidor;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class Servidor {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 8080;

        Server server = ServerBuilder
            .forPort(port)
            .addService(new ServidorImpl())
            .build();

        server.start();

        System.out.println("Servidor iniciado...");
        System.out.println("Escuchando en el puerto: " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                System.out.println("Recibiendo solicitud de apagado...");
                server.shutdown();
                System.out.println("Servidor detenido");
            }
        });

        server.awaitTermination();
    }
}


