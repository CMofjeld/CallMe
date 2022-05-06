export default class ApiHelper {

    callApi(endpoint, method, args) {
        return new Promise((resolve, reject) => {
            let body, formData = null;
            let headers = { 'Accept': '*/*' };

            if (args) {
                if (args.query) {
                    endpoint += `?${args.query}`;
                }
                else if (args.id) {
                    endpoint += `/${args.id}`;
                }

                if (args.body) {
                    body = args.body;
                    headers = { 'Content-Type': 'application/json', ...headers };
                }
                else if (args.formData) {
                    formData = args.formData;
                }

                if (args.token) {
                    headers = { "Authorization": "Bearer " + args.token, ...headers };
                }
            }

            fetch(
                endpoint,
                {
                    headers: headers,
                    method,
                    body: !formData ? body : formData
                })
                .then(response => {
                    return resolve(response);
                })
                .catch(error => {
                    return reject(error);
                });
        });
    }
}
 
 