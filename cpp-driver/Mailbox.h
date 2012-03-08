#ifndef __MAILBOX_INCLUDED__
#define __MAILBOX_INCLUDED__

#include <pthread.h>

using namespace std;

/**
 * Threadsafe single item mailbox.
 */
template <class T >
class Mailbox {

 public:
  ~Mailbox() {
    pthread_mutex_lock(&mutex_);
    if (posted_ != NULL) {
      delete posted_;
    }
    pthread_mutex_unlock(&mutex_);
    pthread_mutex_destroy(&mutex_);
    pthread_cond_destroy(&condition_);
  }

  Mailbox() {
    pthread_mutex_init(&mutex_, NULL);
    pthread_cond_init(&condition_, NULL);
    posted_ = NULL;
  }


  /**
   * Returns an item posted to the mailbox by a producer thread, blocking
   * this thread until an item is available.
   * The caller takes ownership of the object returned.
   */
  T* blockingFetch() {
    T* tmp = NULL;
    pthread_mutex_lock(&mutex_);
    while (posted_ == NULL) {
      pthread_cond_wait(&condition_, &mutex_);
    }
    tmp = posted_;
    posted_ = NULL;
    pthread_mutex_unlock(&mutex_);
    return tmp;
  }

  /**
   * Called by a producer thread to
   * post an item into the mailbox causing any previously posted item which
   * has not yet been collected by a consumer thread to be destroyed.
   * The mailbox takes ownership of the item passed in.
   * @param toPost the object to be posted in the mailbox which can be fetched
   *               by a consumer thread.
   */
  void post(T* toPost) {
    pthread_mutex_lock(&mutex_);
    if (posted_ != NULL) {
      delete posted_;
    }
    posted_ = toPost;
    pthread_cond_signal(&condition_);
    pthread_mutex_unlock(&mutex_);
  }


 private:
  T* posted_;
  pthread_mutex_t mutex_;
  pthread_cond_t condition_;
};


#endif
