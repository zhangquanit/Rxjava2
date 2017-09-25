/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.mobile.social.rxjava.exceptions;

import com.alipay.mobile.social.rxjava.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an exception that is a composite of one or more other exceptions. A {@code CompositeException}
 * does not modify the structure of any exception it wraps, but at print-time it iterates through the list of
 * Throwables contained in the composite in order to print them all.
 * <p>
 * Its invariant is to contain an immutable, ordered (by insertion order), unique list of non-composite
 * exceptions. You can retrieve individual exceptions in this list with {@link #getExceptions()}.
 * <p>
 * The {@link #printStackTrace()} implementation handles the StackTrace in a customized way instead of using
 * {@code getCause()} so that it can avoid circular references.
 * <p>
 * If you invoke {@link #getCause()}, it will lazily create the causal chain but will stop if it finds any
 * Throwable in the chain that it has already seen.
 */
public final class CompositeException extends RuntimeException {

    private static final long serialVersionUID = 3026362227162912146L;

    private final List<Throwable> exceptions;
    private final String message;
    private Throwable cause;

    /**
     * Constructs a CompositeException with the given array of Throwables as the
     * list of suppressed exceptions.
     *
     * @param exceptions the Throwables to have as initially suppressed exceptions
     * @throws IllegalArgumentException if <code>exceptions</code> is empty.
     */
    public CompositeException(@NonNull Throwable... exceptions) {
        this(exceptions == null ?
                Collections.singletonList(new NullPointerException("exceptions was null")) : Arrays.asList(exceptions));
    }

    /**
     * Constructs a CompositeException with the given array of Throwables as the
     * list of suppressed exceptions.
     *
     * @param errors the Throwables to have as initially suppressed exceptions
     * @throws IllegalArgumentException if <code>errors</code> is empty.
     */
    public CompositeException(@NonNull Iterable<? extends Throwable> errors) {
        Set<Throwable> deDupedExceptions = new LinkedHashSet<Throwable>();
        List<Throwable> localExceptions = new ArrayList<Throwable>();
        if (errors != null) {
            for (Throwable ex : errors) {
                if (ex instanceof CompositeException) {
                    deDupedExceptions.addAll(((CompositeException) ex).getExceptions());
                } else if (ex != null) {
                    deDupedExceptions.add(ex);
                } else {
                    deDupedExceptions.add(new NullPointerException("Throwable was null!"));
                }
            }
        } else {
            deDupedExceptions.add(new NullPointerException("errors was null"));
        }
        if (deDupedExceptions.isEmpty()) {
            throw new IllegalArgumentException("errors is empty");
        }
        localExceptions.addAll(deDupedExceptions);
        this.exceptions = Collections.unmodifiableList(localExceptions);
        this.message = exceptions.size() + " exceptions occurred. ";
    }

    /**
     * Retrieves the list of exceptions that make up the {@code CompositeException}.
     *
     * @return the exceptions that make up the {@code CompositeException}, as a {@link List} of {@link Throwable}s
     */
    @NonNull
    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    @NonNull
    public String getMessage() {
        return message;
    }

    @Override
    @NonNull
    public synchronized Throwable getCause() { // NOPMD
        if (cause == null) {
            // we lazily generate this causal chain if this is called
            CompositeExceptionCausalChain localCause = new CompositeExceptionCausalChain();
            Set<Throwable> seenCauses = new HashSet<Throwable>();

            Throwable chain = localCause;
            for (Throwable e : exceptions) {
                if (seenCauses.contains(e)) {
                    // already seen this outer Throwable so skip
                    continue;
                }
                seenCauses.add(e);

                List<Throwable> listOfCauses = getListOfCauses(e);
                // check if any of them have been seen before
                for (Throwable child : listOfCauses) {
                    if (seenCauses.contains(child)) {
                        // already seen this outer Throwable so skip
                        e = new RuntimeException("Duplicate found in causal chain so cropping to prevent loop ...");
                        continue;
                    }
                    seenCauses.add(child);
                }

                // we now have 'e' as the last in the chain
                try {
                    chain.initCause(e);
                } catch (Throwable t) { // NOPMD
                    // ignore
                    // the JavaDocs say that some Throwables (depending on how they're made) will never
                    // let me call initCause without blowing up even if it returns null
                }
                chain = getRootCause(chain);
            }
            cause = localCause;
        }
        return cause;
    }

    static final class CompositeExceptionCausalChain extends RuntimeException {
        private static final long serialVersionUID = 3875212506787802066L;
        /* package-private */static final String MESSAGE = "Chain of Causes for CompositeException In Order Received =>";

        @Override
        public String getMessage() {
            return MESSAGE;
        }
    }

    private List<Throwable> getListOfCauses(Throwable ex) {
        List<Throwable> list = new ArrayList<Throwable>();
        Throwable root = ex.getCause();
        if (root == null || root == ex) {
            return list;
        } else {
            while (true) {
                list.add(root);
                Throwable cause = root.getCause();
                if (cause == null || cause == root) {
                    return list;
                } else {
                    root = cause;
                }
            }
        }
    }

    /**
     * Returns the root cause of {@code e}. If {@code e.getCause()} returns {@code null} or {@code e}, just return {@code e} itself.
     *
     * @param e the {@link Throwable} {@code e}.
     * @return The root cause of {@code e}. If {@code e.getCause()} returns {@code null} or {@code e}, just return {@code e} itself.
     */
    private Throwable getRootCause(Throwable e) {
        Throwable root = e.getCause();
        if (root == null || cause == root) {
            return e;
        }
        while (true) {
            Throwable cause = root.getCause();
            if (cause == null || cause == root) {
                return root;
            }
            root = cause;
        }
    }
}
