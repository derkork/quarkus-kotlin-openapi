import http from 'k6/http'
import {check, sleep} from 'k6'

export default function () {
    const data =
        {
            "title": "Iron Man",
            "releaseDate": "2008-05-02",
            "genres": [
                "action",
                "scifi"
            ],
            "duration": 126,
            "cast": [
                {
                    "character": "Tony Stark",
                    "actor": "Robert Downey Jr.",
                    "type": "Person"
                },
                {
                    "character": "Virginia Potts",
                    "actor": "Gwyneth Paltrow",
                    "type": "Person"
                }
            ]
        }

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    }

    let res = http.post('http://localhost:8080/movies', JSON.stringify(data), params)

    check(res, {'success create': (r) => r.status === 200})
}
