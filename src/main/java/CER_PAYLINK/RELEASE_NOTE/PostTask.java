package CER_PAYLINK.RELEASE_NOTE;

import CER_PAYLINK.PostPaylink;

import java.util.concurrent.Callable;

// Clase de ejemplo para la tarea de publicación
class PostTask implements Callable<String> {
    private String token;
    private PostPaylink initOperation;

    public PostTask(String token, PostPaylink initOperation) {
        this.token = token;
        this.initOperation = initOperation;
    }

    @Override
    public String call() throws Exception {
        // Esto se ejecuta en un hilo del pool
        String id = initOperation.postPaylink(token);
        System.out.println("Hilo: " + Thread.currentThread().getName() + " - Publicado ID: " + id);
        return id;
    }
}