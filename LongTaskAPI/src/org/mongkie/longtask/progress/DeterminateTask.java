/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.longtask.progress;

import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface DeterminateTask {

    public boolean isProgressDialogEnabled();

    public void setTaskHandle(Handle handle);

    public int getWorkunits();

    public static final class Handle {

        private final ProgressHandle handle;
        private int workunits;

        public Handle(ProgressHandle handle, int workunits) {
            this.handle = handle;
            if (workunits > 0) {
                switchToDeterminate(workunits);
            } else {
                switchToIndeterminate();
            }
        }

        public void progress(int workunit) {
            if (workunit < workunits) {
                handle.progress(workunit);
            }
        }

        public void progress(String message, int workunit) {
            if (workunit < workunits) {
                handle.progress(message, workunit);
            }
        }

        public void switchToDeterminate(int workunits) {
            if (workunits > 0) {
                this.workunits = workunits;
                handle.switchToDeterminate(workunits);
            }
        }

        public void switchToIndeterminate() {
            this.workunits = 0;
            handle.switchToIndeterminate();
        }
    }
}
