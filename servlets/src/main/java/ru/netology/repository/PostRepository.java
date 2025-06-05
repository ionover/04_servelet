package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PostRepository {

    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    // Используем множество removedIds для пометки «удалённых» постов
    private final Set<Long> removedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public List<Post> all() {
        return posts.entrySet().stream()
                .filter(entry -> !removedIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public Optional<Post> getById(long id) {
        if (removedIds.contains(id) || !posts.containsKey(id)) {
            throw new NotFoundException("Post with id " + id + " not found");
        }
        return Optional.of(posts.get(id));
    }


    public Post save(Post post) {
        if (post.getId() == 0) {
            long newId = idCounter.incrementAndGet();
            post.setId(newId);
            posts.put(newId, post);
        } else {
            // Если такого ID нет или он уже удалён → 404
            if (!posts.containsKey(post.getId()) || removedIds.contains(post.getId())) {
                throw new NotFoundException("Post with id " + post.getId() + " not found");
            }
            // Иначе обычное обновление
            posts.put(post.getId(), post);
            idCounter.updateAndGet(current -> Math.max(current, post.getId()));
        }
        return post;
    }

    public void removeById(long id) {
        if (!posts.containsKey(id) || removedIds.contains(id)) {
            throw new NotFoundException("Post with id " + id + " not found");
        }
        removedIds.add(id);
    }
}
