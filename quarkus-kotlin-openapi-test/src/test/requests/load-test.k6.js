import http from 'k6/http'
import {check, sleep} from 'k6'

export default function () {
    const data =
        {
            "statusRequired": "second",
            "itemsRequired": []
        }

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    }

    let res = http.post('http://localhost:8080/features/json/required/object', JSON.stringify(data), params)

    check(res, {'success create': (r) => r.status === 200})
}
