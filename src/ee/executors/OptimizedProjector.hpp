/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef EXECUTORS_OPTIMIZED_PROJECTOR_HPP
#define EXECUTORS_OPTIMIZED_PROJECTOR_HPP

#include <set>
#include <vector>

#include "boost/scoped_ptr.hpp"

#include "expressions/abstractexpression.h"

namespace voltdb {

// Forward declarations
class TableTuple;
class TupleSchema;
class ProjectStep;
struct StepComparator;
typedef std::set<ProjectStep, StepComparator> ProjectStepSet;

/**
 * A class that accepts a list of expressions to be projected
 * into a temp table, and produces (internally) a set of steps
 * to perform the projection.  Tuple value expressions are
 * replaced with memcpys.  Where possible, copies of adjacent
 * fields are coalesced into a single call to memcpy.
 */
class OptimizedProjector {
public:

    /** Produce an optimized projector for the given set of expressions.
     * Expressions are assumed to be in the order they will be placed in the
     * destination tuple.  I.e., exprs[0] will go into the first field, etc.
     *
     * To get the optimized projection, call the optimize method before
     * calling exec.
     */
    OptimizedProjector(const std::vector<AbstractExpression*>& exprs);

    /** Default constructor.  Produces an empty Projector that does nothing. */
    OptimizedProjector();

    /** Copy constructor. */
    OptimizedProjector(const OptimizedProjector& that);

    /** This destructor is required for forward declarations to be useful */
    ~OptimizedProjector();

    /** Assignment operator */
    OptimizedProjector& operator=(const OptimizedProjector& rhs);

    /** Add a step to this projection */
    void insertStep(AbstractExpression *expr, int dstFieldIndex);

    /** Optimize the projection into as few mem copies as possible */
    void optimize(const TupleSchema* dstSchema, const TupleSchema* srcSchema);

    /** Perform the projection on a destination tuple. */
    void exec(TableTuple& dstTuple, const TableTuple& srcTuple) const;

    /** For testing, re-order the target fields so mem copies must be broken up. */
    void permuteOnIndexBit(int numBits, int bitToFlip);

    /** The number of steps needed to perform this projection */
    size_t numSteps() const;

    /** For testing, return an exppression for each step.  (This must be done before optimizing,
     * before expressions are replaced with mem copies.)*/
    std::vector<AbstractExpression*> exprs() const;

private:

    boost::scoped_ptr<ProjectStepSet> m_steps;

};

} // end namespace voltdb

#endif
