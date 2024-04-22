package grpcjavaaudio.servidor;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.ByteString;
import com.proto.audio.Audio.DataChunkResponse;
import com.proto.audio.Audio.DownloadFileRequest;
import com.proto.audio.AudioServiceGrpc.AudioServiceImplBase;

import io.grpc.stub.StreamObserver;

public class ServidorImpl extends AudioServiceImplBase {
    @Override
    public void downloadAudio(DownloadFileRequest request, StreamObserver<DataChunkResponse> responseObserver) {
        String fileName = "/" + request.getName();
        System.out.println("\n\nEnviando el archivo: " + request.getName());

        InputStream fileStream = ServidorImpl.class.getResourceAsStream(fileName);

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int length;

        try {
            while((length = fileStream.read(buffer, 0, bufferSize)) != -1){
                DataChunkResponse response = DataChunkResponse.newBuilder()
                .setData(ByteString.copyFrom(buffer, 0, length))
                .build();

                System.out.print(".");
                responseObserver.onNext(response);
            }
        } catch (IOException ex) {
            System.out.println("No se pudo obtener el archivo " + fileName);
        }

        responseObserver.onCompleted();
    }
}


