# Thread safety

* [What is thread safety?](#21-what-is-thread-safety)
    * [Example: a stateless servlet](#211-example-a-stateless-servlet)
* [Atomicity](#22-atomicity)
    * [Race condition](#221-race-condition)
    * [Example: race conditions in lazy initialization](#222-example-race-conditions-in-lazy-initialization)
    * [Compound actions](#223-compound-actions)
* [Locking](#23-locking)

Threads and locks are just *methods* for achieving thread-safe code.

Writing thread-safe
code is, at its core, about managing access to state, and in particular to shared,
mutable state.

Object's state = 'its data' (state variables (instance or static fields)).
In fact, it is all the data that can affect its behavior.

Whether an object needs to be thread-safe depends on whether it will be accessed from
multiple threads. Making an object thread-safe requires synchronization to coordinate
access to its mutable state.

`Fail in coordination = data corruption + other undesirable consquences`.

**RULE:** `Whenever more than one thread accesses a given state variable, and one of them might
write to it, they all must coordinate their access to it using synchronization.`

What are the mechanisms?

* `synchronized` keyword (exclusive locking);
* `volatile` variables;
* explicit locks;
* atomic variables.

If multiple threads access the same mutable state variable without appropriate synchronization, `your program is broken`
.
There are three ways to fix it:

* Don't share the state variable across threads;
* Make the state variable immutable;
* Use synchronization whenever accessing the state variable.

**RULE:** `It is far easier to design a class
to be thread-safe than to retrofit it for thread safety later`

**RULE:** `When designing thread-safe classes, good object-oriented techniques -
encapsulation, immutability, and clear specification of invariants — are
your best friends.`

It is always a good practice first to make your code right, and then
make it fast. Even then, pursue optimization only if your performance measurements and requirements tell you that
you must, and if those same measurements
tell you that your optimizations actually made a difference under realistic conditions.

A program that consists entirely of thread-safe classes may not be thread-safe, and a thread-safe
program may contain classes that are not thread-safe.

## 2.1 What is thread safety?

---

**Correctness concept:** `Class conforms to its specification.
Good specification defines invariants constraining an object's state
and postconditions describing the effects of its operations.`

**IMPORTANT:** `A class is thread-safe if it behaves correctly when accessed from multiple
threads, regardless of the scheduling or interleaving of the execution of
those threads by the runtime environment, and with no additional synchronization or other coordination on the part of
the calling code.`

No set of operations performed sequentially or concurrently on instances of a thread-safe class can
cause an instance to be in an invalid state.

`Thread-safe classes encapsulate any needed synchronization so that
clients need not provide their own.`

### 2.1.1 Example: a stateless servlet

---

Very often, thread-safety requirements stem not
from a decision to use threads directly but from a decision to use a facility like the
Servlets framework.

Stateless servlet:

```java

@ThreadSafe
public class StatelessFactorizer implements Servlet {
    public void service(ServletRequest req, ServletResponse resp) {
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = factor(i);
        encodeIntoResponse(resp, factors);
    }
}
```

`Stateless objects are always thread-safe.`

## 2.2 Atomicity

--- 

Servlet that counts requests without the necessary synchronization.
*Don’t do this*

```java

@NotThreadSafe
public class UnsafeCountingFactorizer implements Servlet {
    private long count = 0;

    public long getCount() {
        return count;
    }

    public void service(ServletRequest req, ServletResponse resp) {
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = factor(i);
        ++count;
        encodeIntoResponse(resp, factors);
    }
}
```

This is an example of a read-modify-write
operation, in which the resulting state is derived from the previous state.

The possibility of incorrect results in the presence of unlucky timing is so important
in concurrent programming that it has a name: `a race condition`.

### 2.2.1 Race condition

---

A race condition occurs when the correctness of a computation depends
on the relative timing or interleaving of multiple threads by the runtime; in other
words, when getting the right answer relies on lucky timing. The most common
type of race condition is `check-then-act`, where a potentially stale observation is
used to make a decision on what to do next.

*Using a potentially stale observation to make a decision or perform a computation* - `check-then-act`: you observe
something to be true (file X doesn't exist) and then take action based on that observation (create X); but in fact the
observation could have become invalid between the time you observed it and the
time you acted on it (someone else created X in the meantime), causing a problem
(unexpected exception, overwritten data, file corruption).

`check----(something may happen and check is no longer actual)----act`

### 2.2.2 Example: race conditions in lazy initialization

---

A common idiom that uses check-then-act is *lazy initialization*.

```java

@NotThreadSafe
public class LazyInitRace {
    private ExpensiveObject instance = null;

    public ExpensiveObject getInstance() {
        if (instance == null)
            instance = new ExpensiveObject();
        return instance;
    }
}
```

### 2.2.3 Compound actions

---

`Operations A and B are atomic with respect to each other if, from the
perspective of a thread executing A, when another thread executes B,
either all of B has executed or none of it has. An atomic operation is one
that is atomic with respect to all operations, including itself, that operate
on the same state.`

```java

@ThreadSafe
public class CountingFactorizer implements Servlet {
    private final AtomicLong count = new AtomicLong(0);

    public long getCount() {
        return count.get();
    }

    public void service(ServletRequest req, ServletResponse resp) {
        BigInteger i = extractFromRequest(req);
        BigInteger[] factors = factor(i);
        count.incrementAndGet();
        encodeIntoResponse(resp, factors);
    }
}
```

Because the state of the servlet is the state of the
counter and the counter is thread-safe, our servlet is once again thread-safe.

`Where practical, use existing thread-safe objects, like AtomicLong, to
manage your class’s state. It is simpler to reason about the possible
states and state transitions for existing thread-safe objects than it is for
arbitrary state variables, and this makes it easier to maintain and verify
thread safety.`

## 2.3 Locking
