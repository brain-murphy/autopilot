/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.table;

import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of timeout, payload format, and retry policy options that may be specified for a table operation
 * request.
 */
public class TableRequestOptions extends RequestOptions {

    /**
     * The interface whose function is used to get the <see cref="EdmType"/> for an entity property
     * given the partition key, row, key, and the property name, if the interface is implemented
     */
    public interface PropertyResolver {

        /**
         * Given the partition key, row, key, and the property name, produces the EdmType
         * 
         * @param pk
         *            A <code>String</code> which represents the partition key.
         * @param rk
         *            A <code>String</code> which represents the row key.
         * @param key
         *            A <code>String</code> which represents the property name.
         * @param value
         *            A <code>String</code> which represents the property value.
         * @return
         *         The {@link EdmType} of the property.
         */
        public EdmType propertyResolver(String pk, String rk, String key, String value);

    }

    /**
     * The interface whose function is used to get the <see cref="EdmType"/> for an entity property
     * given the partition key, row, key, and the property name, if the interface is implemented
     */
    private PropertyResolver propertyResolver;

    /**
     * The <see {@link TablePayloadFormat} that is used for any table accessed with this {@link TableRequest} object.
     * 
     * Default is Json Minimal Metadata.
     */
    private TablePayloadFormat payloadFormat;

    /**
     * Creates an instance of the <code>TableRequestOptions</code>
     */
    public TableRequestOptions() {
        super();
    }

    /**
     * Creates an instance of the <code>RequestOptions</code> class by copying values from another
     * <code>TableRequestOptions</code> instance.
     * 
     * @param other
     *            A <code>TableRequestOptions</code> object that represents the request options to copy.
     */
    public TableRequestOptions(final TableRequestOptions other) {
        super(other);
        if (other != null) {
            this.setTablePayloadFormat(other.getTablePayloadFormat());
            this.setPropertyResolver(other.getPropertyResolver());
        }
    }

    /**
     * Reserved for internal use. Initializes the values for this <code>TableRequestOptions</code> instance, if they are
     * currently <code>null</code>, using the values specified in the {@link CloudTableClient} parameter.
     * 
     * @param options
     *            A {@link TableRequestOptions} object which represents the input options to copy from when applying defaults.
     * @param client
     *            A {@link CloudTableClient} object from which to copy the timeout and retry policy.
     *
     * @return A {@link TableRequestOptions} object.
     * 
     */
    protected static final TableRequestOptions applyDefaults(final TableRequestOptions options,
            final CloudTableClient client) {
        TableRequestOptions modifiedOptions = new TableRequestOptions(options);
        TableRequestOptions.populateRequestOptions(modifiedOptions, client.getDefaultRequestOptions());
        return TableRequestOptions.applyDefaultsInternal(modifiedOptions, client);
    }

    private static final TableRequestOptions applyDefaultsInternal(final TableRequestOptions modifiedOptions,
            CloudTableClient client) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        RequestOptions.applyBaseDefaultsInternal(modifiedOptions);
        if (modifiedOptions.getTablePayloadFormat() == null) {
            modifiedOptions.setTablePayloadFormat(TablePayloadFormat.Json);
        }

        return modifiedOptions;
    }

    /**
     * Populates any null fields in the first requestOptions object with values from the second requestOptions object.
     * 
     * @param modifiedOptions
     *            A {@link TableRequestOptions} object from which to copy options.
     * @param clientOptions
     *            A {@link TableRequestOptions} object where options will be copied.
     *            
     * @return A {@link RequestOptions} object.
     */
    private static final RequestOptions populateRequestOptions(TableRequestOptions modifiedOptions,
            final TableRequestOptions clientOptions) {
        RequestOptions.populateRequestOptions(modifiedOptions, clientOptions, false);
        if (modifiedOptions.getTablePayloadFormat() == null) {
            modifiedOptions.setTablePayloadFormat(clientOptions.getTablePayloadFormat());
        }

        if (modifiedOptions.getPropertyResolver() == null) {
            modifiedOptions.setPropertyResolver(clientOptions.getPropertyResolver());
        }

        return modifiedOptions;
    }

    /**
     * Gets the {@link TablePayloadFormat} to be used. For more information about {@link TablePayloadFormat} defaults,
     * see {@link #setTablePayloadFormat(TablePayloadFormat)}.
     * 
     * @return
     *         The {@link TablePayloadFormat} used by this {@link TableRequest}.
     */
    public TablePayloadFormat getTablePayloadFormat() {
        return this.payloadFormat;
    }

    /**
     * Gets the interface that contains a function which is used to get the <see cref="EdmType"/> for an entity property
     * given the partition key, row, key, and the property name. For more information about the {@link PropertyResolver}
     * defaults, see {@link #setPropertyResolver(PropertyResolver)}.
     * 
     * @return
     *         The current {@link PropertyResolver} object.
     */
    public PropertyResolver getPropertyResolver() {
        return this.propertyResolver;
    }

    /**
     * Sets the {@link TablePayloadFormat} to be used.
     * <p>
     * The default {@link TablePayloadFormat} is set in the client and is by default {@link TablePayloadFormat#Json}.
     * You can change the {@link TablePayloadFormat} on this request by setting this property. You can also change the
     * value on the {@link TableServiceClient#getDefaultRequestOptions()} object so that all subsequent requests made
     * via the service client will use that {@link TablePayloadFormat}.
     * 
     * @param payloadFormat
     *            Specifies the {@link TablePayloadFormat} to set.
     */
    public void setTablePayloadFormat(TablePayloadFormat payloadFormat) {
        this.payloadFormat = payloadFormat;
    }

    /**
     * Sets the interface that contains a function which is used to get the <see cref="EdmType"/> for an entity property
     * given the partition key, row, key, and the property name.
     * <p>
     * The default {@link PropertyResolver} is set in the client and is by default null, indicating not to use a
     * property resolver. You can change the {@link PropertyResolver} on this request by setting this property. You can
     * also change the value on the {@link TableServiceClient#getDefaultRequestOptions()} object so that all subsequent
     * requests made via the service client will use that {@link PropertyResolver}.
     * 
     * @param propertyResolver
     *            Specifies the {@link PropertyResolver} to set.
     */
    public void setPropertyResolver(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
    }
}
