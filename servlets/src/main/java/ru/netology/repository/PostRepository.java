package ru.netology.repository;

import ru.netology.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PostRepository {

    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public List<Post> all() {
        return new ArrayList<>(posts.values());
    }

    public Optional<Post> getById(long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            long newId = idCounter.incrementAndGet();
            post.setId(newId);
            posts.put(newId, post);
        } else {
            posts.put(post.getId(), post);
            idCounter.updateAndGet(currentValue -> Math.max(currentValue, post.getId()));
        }

        return post;
    }

    public void removeById(long id) {
        posts.remove(id);
    }
}
