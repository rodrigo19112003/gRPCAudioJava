package grpcjavaaudio.cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import com.proto.audio.AudioServiceGrpc;
import com.proto.audio.Audio.DownloadFileRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Cliente {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        String name;

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

        name = "anyma.wav";
        streamWav(managedChannel, name, 48000F);
        // name = "tiesto.mp3";
        // ByteArrayInputStream streamMP3 = downloadFile(managedChannel, name);
        // playMp3(streamMP3, name);
        // try {
        //     streamMP3.close();
        // } catch (IOException e) {
        // }

        System.out.println("Apagando...");
        managedChannel.shutdown();
    }

    public static void streamWav(ManagedChannel managedChannel, String name, float sampleRate) {
        try {
            AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 2, true, false);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            
            AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(managedChannel);
            DownloadFileRequest request = DownloadFileRequest.newBuilder().setName(name).build();

            int bufferSize = 1024;
            System.out.println("Reproduciendo el archivo: " + name);

            stub.downloadAudio(request).forEachRemaining(response -> {
                try {
                    sourceDataLine.write(response.getData().toByteArray(), 0, bufferSize);
                    System.out.print(".");
                } catch (Exception e) {
                }
            });
            System.out.println("Recepcion de datos correcta.");
            System.out.println("Reproduccion terminada.\n\n");

            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ByteArrayInputStream downloadFile(ManagedChannel managedChannel, String name){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(managedChannel);
        DownloadFileRequest request = DownloadFileRequest.newBuilder().setName(name).build();

        System.out.println("Recibiendo el archivo: " + name);

        stub.downloadAudio(request).forEachRemaining(response -> {
            try {
                stream.write(response.getData().toByteArray());
                System.out.print(".");
            } catch (Exception e) {
                System.out.println("No se pudo obtener el archivo de audio." + e.getMessage());
            }
        });

        System.out.println("\nRecepcion de datos correcta.\n\n");
        return new ByteArrayInputStream(stream.toByteArray());
    }

    public static void playWav(ByteArrayInputStream inputStream, String name){
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            System.out.println("Reproduciendo el archivo: " + name + "...\n\n");

            clip.start();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            clip.stop();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e){
            e.printStackTrace();
        }
    }
    public static void playMp3(ByteArrayInputStream inputStream, String name){
        try {
            System.out.println("Reproduciendo el archivo: " + name + "...\n\n");
            Player player = new Player(inputStream);
            player.play();
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
    }
}
